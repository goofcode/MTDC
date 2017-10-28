#include <jni.h>

#include <vector>
#include <string>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include <android/log.h>

extern "C"
JNIEXPORT void JNICALL
Java_kr_ac_cau_goofcode_MTDC_Streamer_convertRGBtoGray(JNIEnv *env, jobject instance,
                                                       jlong matAddrInput, jlong matAddrResult) {

    cv::Mat &mat_input = *(cv::Mat *) matAddrInput;
    cv::Mat &mat_result = *(cv::Mat *) matAddrResult;
    cv::cvtColor(mat_input, mat_result, CV_RGBA2GRAY);
}

std::vector<std::vector<cv::Point2f>> detectMarkers(const cv::Mat &matInput) {

    const int MIN_MARKER_SIZE = 500;
    const int MAX_MARKER_SIZE = 50000;

    const int MARKER_LENGTH = 80;
    const int MARKER_CELL_SIZE = 8;

    //to gray scale and binary
    cv::Mat mat_gray_input, mat_binary_input;
    cv::cvtColor(matInput, mat_gray_input, CV_BGR2GRAY);
    //cv::adaptiveThreshold(mat_result, mat_result, 255, cv::ADAPTIVE_THRESH_GAUSSIAN_C, cv::THRESH_BINARY_INV, 89, 7);
    cv::threshold(mat_gray_input, mat_binary_input, 125, 255,
                  cv::THRESH_BINARY_INV | cv::THRESH_OTSU);

    //contour searching
    std::vector<std::vector<cv::Point>> contours;
    cv::findContours(mat_binary_input.clone(), contours, cv::RETR_LIST, cv::CHAIN_APPROX_SIMPLE);

    //contour approximation
    std::vector<std::vector<cv::Point2f>> quad_contours;
    std::vector<cv::Point2f> approx;
    for (size_t i = 0; i < contours.size(); i++) {
        cv::approxPolyDP(cv::Mat(contours[i]), approx,
                         cv::arcLength(cv::Mat(contours[i]), true) * 0.05, true);
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

    const std::vector<cv::Point2f> square({{0,                 0},
                                           {MARKER_LENGTH - 1, 0},
                                           {MARKER_LENGTH - 1, MARKER_LENGTH - 1},
                                           {0,                 MARKER_LENGTH - 1}});

    std::vector<std::vector<cv::Point2f>> markers;
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
            int inc = (y == 0 || y == 7) ? 1 : 7;
            for (int x = 0; x < 8; x += inc) {
                cv::Mat cell = mat_marker(
                        cv::Rect(x * cell_size, y * cell_size, cell_size, cell_size));
                if (countNonZero(cell) > (cell_size * cell_size) / 2) num_white_cell++;
            }
        }

        // if boundary cells are all black, recognize as marker
        if (num_white_cell == 0)
            markers.push_back(quad_contours[i]);
    }

    return markers;
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_kr_ac_cau_goofcode_MTDC_Streamer_getTrackingControlData(
        JNIEnv *env, jobject instance, jlong matAddrInput, jlong matAddrResult) {

    cv::Mat &mat_input = *(cv::Mat *) matAddrInput;
    cv::Mat &mat_result = *(cv::Mat *) matAddrResult;
    mat_result = mat_input;

    auto markers = detectMarkers(mat_input);

    //if no markers, return stay control
    if (markers.size() == 0) {
        int stay_ctrl[4] = {128, 128, 128, 128};
        jintArray ctrl_array = env->NewIntArray(4);
        env->SetIntArrayRegion(ctrl_array, 0, 4, stay_ctrl);
        return ctrl_array;
    }


    int target_num = 0;

    for (size_t i = 0; i < markers.size(); i++) {
        if (i == target_num)
            cv::polylines(mat_result, std::vector<cv::Point>(markers[i].begin(), markers[i].end()),
                          true, cv::Scalar(255, 0, 0), 2);
        else
            cv::polylines(mat_result, std::vector<cv::Point>(markers[i].begin(), markers[i].end()),
                          true, cv::Scalar(0, 0, 255));
    }


    std::vector<cv::Point> target;
    cv::Mat(markers[target_num]).convertTo(target, cv::Mat(target).type());

    int cx = (int) ((target[0].x + target[1].x + target[2].x + target[3].x) / 4.0);
    int cy = (int) ((target[0].y + target[1].y + target[2].y + target[3].y) / 4.0);

    //sort target four points in clockwise order
    std::sort(target.begin(), target.end(),
              [&cx, &cy](const cv::Point &a, const cv::Point b) -> bool {
                  return ((a.x < cx) << 1 + (a.y < cy)) < ((b.x < cx) << 1 + (b.y < cy));
              }
    );

    int r_height = (int) (cv::norm(target[1] - target[0]));
    int l_height = (int) (cv::norm(target[2] - target[3]));
    //int b_width = (int) (cv::norm(target[1] - target[2]));
    int area = (int) (abs(target[0].x * target[1].y - target[1].x * target[0].y +
                          target[1].x * target[2].y - target[2].x * target[1].y +
                          target[2].x * target[3].y - target[3].x * target[2].y +
                          target[3].x * target[0].y - target[0].x * target[3].y) / 2.0);


    __android_log_print(ANDROID_LOG_INFO, "Target Pos",
                        "c(%d, %d), area: %d\t %d %d \tt", cx, cy, area, r_height, l_height);

    /* CALCULATE Control data */
    const int PROPER_DIST_SIZE_MAX_L1 = 6000;
    const int PROPER_DIST_SIZE_MAX_L2 = 7500;
    const int PROPER_DIST_SIZE_MIN_L1 = 4500;
    const int PROPER_DIST_SIZE_MIN_L2 = 3000;

    const int CENTER_X = 320;
    const int CENTER_Y = 240;
    const int HORIZONTAL_LIMIT = 45;
    const int HORIZONTAL_MAX_LIMIT = 80;
    const int VERTICAL_MAXLIMIT = 50;
    const int VERTICAL_LIMIT = 30;
    const double ROTATE_LIMIT = 1.07;

    const int HORIZONTAL_LMOVE = 22;
    const int HORIZONTAL_RMOVE = 13;
    const int HORIZONTAL_MAX_LMOVE = 35;
    const int HORIZONTAL_MAX_RMOVE = 25;


    int thro = 128, rudd = 128, elev = 128, aile = 128;

    std::string log_message;

    //front and back move
    if (area < PROPER_DIST_SIZE_MIN_L2) {elev += 13; log_message += "front\t";}
    else if(area < PROPER_DIST_SIZE_MIN_L1){elev += 5; log_message += "front\t";}
    else if(PROPER_DIST_SIZE_MAX_L2 < area) {elev -= 18; log_message += "back \t";}
    else if (PROPER_DIST_SIZE_MAX_L1 < area) {elev -= 7; log_message += "back \t";}

    //up and down move
    if (cy < CENTER_Y - VERTICAL_MAXLIMIT) {thro += 20; log_message += "upStrong \t";}
    else if (cy < CENTER_Y - VERTICAL_LIMIT) {thro += 10; log_message += "up \t";}
    else if (cy > CENTER_Y + VERTICAL_MAXLIMIT) {thro -= 25; log_message += "downStrong \t";}
    else if (cy > CENTER_Y + VERTICAL_LIMIT) {thro -= 15; log_message += "down \t";}

    //left and right move
    if((double)r_height/l_height > ROTATE_LIMIT){
        rudd -= 5; aile -= HORIZONTAL_LMOVE; log_message += "clockwise\t";
    }
    else if ((double)l_height/r_height > ROTATE_LIMIT){
        rudd += 5; aile += HORIZONTAL_RMOVE; log_message += "counter-clk\t";
    }
    else {
        if (cx < CENTER_X - HORIZONTAL_MAX_LIMIT) {aile -= HORIZONTAL_MAX_LMOVE; log_message += "leftStrong\t";}
        else if(cx < CENTER_X - HORIZONTAL_LIMIT) {aile -= HORIZONTAL_LMOVE; log_message += "left\t";}
        else if (cx > CENTER_X + HORIZONTAL_MAX_LIMIT) {aile += HORIZONTAL_MAX_RMOVE; log_message += "rightStrong\t";}
        else if (cx > CENTER_X + HORIZONTAL_LIMIT) {aile += HORIZONTAL_RMOVE; log_message += "right\t";}
    }
    __android_log_print(ANDROID_LOG_INFO, "Moving", "%s", log_message.c_str());


    int track_ctrl[4] = {thro, rudd, elev, aile};
    jintArray track_ctrl_array = env->NewIntArray(4);
    env->SetIntArrayRegion(track_ctrl_array, 0, 4, track_ctrl);
    return track_ctrl_array;
}