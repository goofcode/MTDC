#include <jni.h>

#include <vector>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

extern "C"
JNIEXPORT void JNICALL
Java_kr_ac_cau_goofcode_MTDC_Streamer_convertRGBtoGray(JNIEnv *env, jobject instance,
                                                       jlong matAddrInput, jlong matAddrResult) {

    cv::Mat &mat_input = *(cv::Mat *) matAddrInput;
    cv::Mat &mat_result = *(cv::Mat *) matAddrResult;

    cv::cvtColor(mat_input, mat_result, CV_RGBA2GRAY);
}

JNIEXPORT void JNICALL
Java_kr_ac_cau_goofcode_MTDC_Streamer_detectMarker(JNIEnv *env, jobject instance,
                                                   jlong matAddrInput, jlong matAddrResult) {

    cv::Mat &mat_input = *(cv::Mat *) matAddrInput;
    cv::Mat &mat_result = *(cv::Mat *) matAddrResult;

    //to gray scale and binary
    cv::cvtColor(mat_input, mat_result, CV_BGR2GRAY);
    cv::adaptiveThreshold(mat_result, mat_result, 255, cv::ADAPTIVE_THRESH_GAUSSIAN_C, cv::THRESH_BINARY_INV, 89, 7);
    //cv::threshold(input_gray_image, binary_image, 125, 255, cv::THRESH_BINARY_INV | cv::THRESH_OTSU);

    //contour searching
    std::vector <std::vector<cv::Point>> contours;
    cv::findContours(mat_result.clone(), contours, cv::RETR_LIST, cv::CHAIN_APPROX_SIMPLE);

    //contour approximation
    std::vector<std::vector<cv::Point2f>> marker;
    std::vector<cv::Point2f> approx;
    for (size_t i = 0; i < contours.size(); i++) {
        cv::approxPolyDP(cv::Mat(contours[i]), approx, cv::arcLength(cv::Mat(contours[i]), true) * 0.05, true);

        if (approx.size() == 4 &&
                std::fabs(cv::contourArea(cv::Mat(approx))) > 1000 &&
                std::fabs(contourArea(cv::Mat(approx))) < 50000 &&
                cv::isContourConvex(cv::Mat(approx)))
        {
            std::vector <cv::Point2f> points;
            for (int j = 0; j < 4; j++)
                points.push_back(cv::Point2f(approx[j].x, approx[j].y));

            //반시계 방향으로 정렬
            cv::Point v1 = points[1] - points[0];
            cv::Point v2 = points[2] - points[0];
            if ( (v1.x * v2.y) - (v1.y * v2.x) < 0.0) swap(points[1], points[3]);

            marker.push_back(points);
        }
    }

    for(auto iter_marker = marker.begin(); iter_marker<marker.end();iter_marker++)
        cv::polylines(mat_result,(*iter_marker), true, cv::Scalar(255,0,0), 2);

    /*
    //perspective transformation을 적용하여 정면샷으로 전환
    //otsu 방법으로 이진화, 흰색과 검은색으로
    //이진화된 이미지를 격자로 분할ex)6*6이면 테두리포함해서
    //64개셀로, 마커 주변 테두리영역의 셀을 검사, 흰색셀이면
    //마커 후보에서 제외, 셀 내의 흰색 픽셀이 절반이상==흰색셀
    std::vector<std::vector<cv::Point2f>> detectedMarkers;
    std::vector<cv::Mat> detectedMarkersImage;
    std::vector<cv::Point2f> square_points;

    const int marker_image_side_length = 80;
    //마커 6*6 크기일 때 검은색 테두리 영역 포함한 크기 8*8
    //이후 단계에서 이미지를 격자로 분할할 시 셀하나의 픽셀너비 10설정시

    //마커 이미지의 한변 길이는 80
    square_points.push_back(cv::Point2f(0, 0));
    square_points.push_back(cv::Point2f(marker_image_side_length - 1, 0));
    square_points.push_back(
            cv::Point2f(marker_image_side_length - 1, marker_image_side_length - 1));
    square_points.push_back(cv::Point2f(0, marker_image_side_length - 1));

    cv::Mat marker_image;
    for (int i = 0; i < marker.size(); i++) {
        std::vector<cv::Point2f> m = marker[i];

        //Mat input_gray_image2 = input_gray_image.clone();
        //Mat markerSubImage = input_gray_image2(cv::boundingRect(m));


        //마커를 사각형형태로 바꿀 perspective transformation matrix를 구한다.
        Mat PerspectiveTransformMatrix = getPerspectiveTransform(m, square_points);

        //perspective transformation을 적용한다.
        warpPerspective(input_gray_image, marker_image, PerspectiveTransformMatrix,
                        Size(marker_image_side_length, marker_image_side_length));

        //otsu 방법으로 이진화를 적용한다.
        threshold(marker_image, marker_image, 125, 255, THRESH_BINARY | THRESH_OTSU);



        //마커의 크기는 6, 검은색 태두리를 포함한 크기는 8
        //마커 이미지 테두리만 검사하여 전부 검은색인지 확인한다.
        int cellSize = marker_image.rows / 8;
        int white_cell_count = 0;
        for (int y = 0; y < 8; y++) {
            int inc = 7; // 첫번째 열과 마지막 열만 검사하기 위한 값

            if (y == 0 || y == 7) inc = 1; //첫번째 줄과 마지막줄은 모든 열을 검사한다.


            for (int x = 0; x < 8; x += inc) {
                int cellX = x * cellSize;
                int cellY = y * cellSize;
                cv::Mat cell = marker_image(Rect(cellX, cellY, cellSize, cellSize));

                int total_cell_count = countNonZero(cell);

                if (total_cell_count > (cellSize * cellSize) / 2)
                    white_cell_count++; //태두리에 흰색영역이 있다면, 셀내의 픽셀이 절반이상 흰색이면 흰색영역으로 본다

            }
        }

        //검은색 태두리로 둘러쌓여 있는 것만 저장한다.
        if (white_cell_count == 0) {
            detectedMarkers.push_back(m);
            Mat img = marker_image.clone();
            detectedMarkersImage.push_back(img);
        }
    }
    //검은테두리 제외한 6*6영역내에 있는 셀들에 흰색개수 카운트
    //흰색1,검은색0으로 6*6배열을 만들어 비트 매트리스를 만든다.
    vector <Mat> bitMatrixs;
    for (int i = 0; i < detectedMarkers.size(); i++) {
        Mat marker_image = detectedMarkersImage[i];

        //내부 6x6에 있는 정보를 비트로 저장하기 위한 변수
        Mat bitMatrix = Mat::zeros(6, 6, CV_8UC1);

        int cellSize = marker_image.rows / 8;
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {
                int cellX = (x + 1) * cellSize;
                int cellY = (y + 1) * cellSize;
                Mat cell = marker_image(cv::Rect(cellX, cellY, cellSize, cellSize));

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
    for (int i = 0; i < detectedMarkers.size(); i++) {
        Mat bitMatrix = bitMatrixs[i];
        vector <Point2f> m = detectedMarkers[i];


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