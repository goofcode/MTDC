#include <jni.h>

#include<iostream>
#include <vector>
#include <string>
#include<iterator>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include <android/log.h>

std::string point2f2string(cv::Point2f point) {
    std::ostringstream ss;
    ss << "[" << point.x << ", " << point.y << "]";
    return std::string(ss.str());
}

extern "C"
JNIEXPORT void JNICALL
Java_kr_ac_cau_goofcode_MTDC_Streamer_convertRGBtoGray(JNIEnv *env, jobject instance,
                                                       jlong matAddrInput, jlong matAddrResult) {

    cv::Mat &mat_input = *(cv::Mat *) matAddrInput;
    cv::Mat &mat_result = *(cv::Mat *) matAddrResult;

    cv::cvtColor(mat_input, mat_result, CV_RGBA2GRAY);
}

extern "C"
JNIEXPORT void JNICALL
Java_kr_ac_cau_goofcode_MTDC_Streamer_detectMarker(JNIEnv *env, jobject instance,
                                                   jlong matAddrInput, jlong matAddrResult) {

    const int MIN_MARKER_SIZE = 500;
    const int MAX_MARKER_SIZE = 50000;

    const int MARKER_LENGTH = 80;
    const int MARKER_CELL_SIZE = 8;

    cv::Mat &mat_input = *(cv::Mat *) matAddrInput;
    cv::Mat &mat_result = *(cv::Mat *) matAddrResult;

    mat_result = mat_input;

    //to gray scale and binary
    cv::Mat mat_gray_input, mat_binary_input;
    cv::cvtColor(mat_input, mat_gray_input, CV_BGR2GRAY);
    //cv::adaptiveThreshold(mat_result, mat_result, 255, cv::ADAPTIVE_THRESH_GAUSSIAN_C, cv::THRESH_BINARY_INV, 89, 7);
    cv::threshold(mat_gray_input, mat_binary_input, 125, 255, cv::THRESH_BINARY_INV | cv::THRESH_OTSU);

    //contour searching
    std::vector<std::vector<cv::Point>> contours;
    cv::findContours(mat_binary_input.clone(), contours, cv::RETR_LIST, cv::CHAIN_APPROX_SIMPLE);

    //contour approximation
    std::vector<std::vector<cv::Point2f>> quad_contours;
    std::vector<cv::Point2f> approx;
    for (size_t i = 0; i < contours.size(); i++) {
        cv::approxPolyDP(cv::Mat(contours[i]), approx, cv::arcLength(cv::Mat(contours[i]), true) * 0.05, true);
        if (approx.size() == 4 &&
            std::fabs(cv::contourArea(cv::Mat(approx))) > MIN_MARKER_SIZE &&
            std::fabs(cv::contourArea(cv::Mat(approx))) < MAX_MARKER_SIZE &&
            cv::isContourConvex(cv::Mat(approx))) {

            std::vector<cv::Point2f> points;
            for (int j = 0; j < 4; j++)
                points.push_back(cv::Point2f(approx[j].x, approx[j].y));

            // sorting in counter clock order
            cv::Point v1 = points[1] - points[0];
            cv::Point v2 = points[2] - points[0];
            if ((v1.x * v2.y) - (v1.y * v2.x) < 0.0) swap(points[1], points[3]);

            quad_contours.push_back(points);
        }
    }

    const std::vector<cv::Point2f> square({{0,0}, {MARKER_LENGTH - 1, 0},
                                           {MARKER_LENGTH - 1, MARKER_LENGTH - 1},
                                           {0, MARKER_LENGTH - 1}});

    std::vector<std::vector<cv::Point2f>> markers;
    std::vector<cv::Mat> mat_markers;
    cv::Mat mat_marker;

    for (int i = 0; i < quad_contours.size(); i++) {
        //transform quad_contours to square
        warpPerspective(mat_gray_input, mat_marker,
                        getPerspectiveTransform(quad_contours[i], square),
                        cv::Size(MARKER_LENGTH, MARKER_LENGTH));
        threshold(mat_marker, mat_marker, 125, 255, cv::THRESH_BINARY | cv::THRESH_OTSU);

        // check boundary cells
        int cell_size = mat_marker.rows / MARKER_CELL_SIZE;
        int num_white_cell = 0;
        for (int y = 0; y < 8; y++) {
            int inc = (y==0 || y==7)? 1 : 7; // 첫번째 열과 마지막 열만 검사하기 위한 값
            for (int x = 0; x < 8; x += inc) {
                cv::Mat cell = mat_marker(cv::Rect(x * cell_size, y * cell_size, cell_size, cell_size));
                if (countNonZero(cell) > (cell_size * cell_size) / 2) num_white_cell++;
            }
        }

        // if boundary cells are all black, recognize as marker
        if (num_white_cell == 0) {
            markers.push_back(quad_contours[i]);
            mat_markers.push_back(mat_marker.clone());
        }
    }

    for (size_t i = 0; i < markers.size(); i++)
        cv::polylines(mat_result, std::vector<cv::Point>(markers[i].begin(),markers[i].end()), true, cv::Scalar(0,0,255));

    /*
    //검은테두리 제외한 6*6영역내에 있는 셀들에 흰색개수 카운트
    //흰색1,검은색0으로 6*6배열을 만들어 비트 매트리스를 만든다.
    vector <Mat> bitMatrixs;
    for (int i = 0; i < markers.size(); i++) {
        Mat mat_marker = mat_markers[i];

        //내부 6x6에 있는 정보를 비트로 저장하기 위한 변수
        Mat bitMatrix = Mat::zeros(6, 6, CV_8UC1);

        int cellSize = mat_marker.rows / 8;
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {
                int cellX = (x + 1) * cellSize;
                int cellY = (y + 1) * cellSize;
                Mat cell = mat_marker(cv::Rect(cellX, cellY, cellSize, cellSize));

                int total_cell_count = countNonZero(cell);


                if (total_cell_count > (cellSize * cellSize) / 2)
                    bitMatrix.at<uchar>(y, x) = 1;
            }
        }

        bitMatrixs.push_back(bitMatrix);

    }
//비트 매트릭스를 바이트 리스트로 바꿉니다. 필요한 바이트 수는 (6*6+8-1)/8
// 5바이트 저장할 수 있는 변수를 선언, 스캔해서 저장
//처음에 비트매트릭스(x,y)=(0,0) 비트값 0이 첫번째 바이트의 8번째에 입력
//왼쪽으로 시프트, 첫번째 바이트의 8번째 비트에 입력하는 것을 반복
    //바이트 단위로 이동하며 같은 연산
    Mat getByteListFromBits(const Mat &bits) {
        // integer ceil
        int nbytes = (bits.cols * bits.rows + 8 - 1) / 8;

        Mat candidateByteList(1, nbytes, CV_8UC1, Scalar::all(0));
        unsigned char currentBit = 0;
        int currentByte = 0;

        uchar *rot0 = candidateByteList.ptr();

        for (int row = 0; row < bits.rows; row++) {
            for (int col = 0; col < bits.cols; col++) {
                // circular shift
                rot0[currentByte] <<= 1;

                // set bit
                rot0[currentByte] |= bits.at<uchar>(row, col);

                currentBit++;
                if (currentBit == 8) {
                    // next byte
                    currentBit = 0;
                    currentByte++;
                }
            }
        }
        return candidateByteList;
    }
    //dictionary에는 마커ID->0도,90도,180도,270도에대한 4개의 바이트 리스트로 저장
    //위에서 구한 바이트 리스트를 검색하면 ID 0번의 첫번째 바이트리스트와 ID41 이 일치
    //이러한 일치 여부로 회전 각도를 알아냄.
    //opencv_contrib / modules / aruco / src / predefined_dictionaries.hpp에 선언 되어 있는
    //배열 DICT_6X6_1000_BYTES[][4][5]를 작성중인 코드로 가져오고 아래 한줄을 선언해주었습니다.
    //1000개중 250개만 dictionary에서 사용합니다.
    Mat dictionary = Mat(250, (6 * 6 + 7) / 8, CV_8UC4, (uchar *) DICT_6X6_1000_BYTES);
    //identiy함수를 사용하여 마커가 발견되었다면 회전 인덱스에 맞게 코너를 회전
    vector<int> markerID;
    vector <vector<Point2f>> final_detectedMarkers;
    for (int i = 0; i < markers.size(); i++) {
        Mat bitMatrix = bitMatrixs[i];
        vector <Point2f> m = markers[i];


        int rotation;
        int marker_id;
        if (!identify(bitMatrix, marker_id, rotation))
            cout << "발견안됨" << endl;
        else {

            if (rotation != 0) {
                //회전을 고려하여 코너를 정렬합니다.
                //마커의 회전과 상관없이 마커 코너는 항상 같은 순서로 저장됩니다.
                std::rotate(m.begin(), m.begin() + 4 - rotation, m.end());
            }

            markerID.push_back(marker_id);
            final_detectedMarkers.push_back(m);
        }
    }
    //identify함수에서는 비트 매트릭스를 바이트 리스트로 변환한 후,
    //dictionary에 저장된 바이트 리스트와  비교하여 매치되는 마커 ID와 회전 인덱스를 구합니다.
    //매치여부는 간단하게 해밍 거리를 계산하여 최소가 될 경우로 했습니다.
    bool identify(const Mat &onlyBits, int &idx, int &rotation) {
        int markerSize = 6;

        //비트 매트릭스를 바이트 리스트로 변환합니다.
        Mat candidateBytes = getByteListFromBits(onlyBits);

        idx = -1; // by default, not found

        //dictionary에서 가장 근접한 바이트 리스트를 찾습니다.
        int MinDistance = markerSize * markerSize + 1;
        rotation = -1;
        for (int m = 0; m < dictionary.rows; m++) {

            //각 마커 ID
            for (unsigned int r = 0; r < 4; r++) {
                int currentHamming = hal::normHamming(
                        dictionary.ptr(m) + r * candidateBytes.cols,
                        candidateBytes.ptr(),
                        candidateBytes.cols);

                //이전에 계산된 해밍 거리보다 작다면
                if (currentHamming < MinDistance) {
                    //현재 해밍 거리와 발견된 회전각도를 기록합니다.
                    MinDistance = currentHamming;
                    rotation = r;
                    idx = m;
                }
            }
        }

        //idx가 디폴트값 -1이 아니면 발견된 것
        return idx != -1;
    }
    */
}