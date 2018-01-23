package co.polarr.polarrcamerademo;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.polarr.renderer.entities.FaceItem;

/**
 * Created by Colin on 2017/12/2.
 */

public class FaceUtil {
    /**
     * get the face features with 106 points input
     */
    public static Map<String, Object> GetFaceFeaturesWithPoints(List<List<PointF>> facePoints, int imageWidth, int imageHeight) {
        Map<String, Object> faces = new HashMap<>();

        if (facePoints == null) {
            return faces;
        }

        List<FaceDetResult> faceDetResults = new ArrayList<>();
        for (List<PointF> points : facePoints) {
            if (points.size() == 106) {
                faceDetResults.add(mapSenstimeToDlib(points));
            }
        }

        faces = createFaceStates(faceDetResults, imageWidth, imageHeight);


        return faces;
    }

    private static Map<String, Object> createFaceStates(List<FaceDetResult> faceDetResults, int bitmapWidth, int bitmapHeight) {
        Map<String, Object> faceStates = new HashMap<>();
        List<FaceItem> faces = new ArrayList<>();
        List<co.polarr.renderer.entities.Context.FaceFeaturesState> faceFeatures = new ArrayList<>();

        faceStates.put("faces", faces);
        faceStates.put("face_features", faceFeatures);

        for (FaceDetResult faceDetResult : faceDetResults) {
            FaceItem faceItem = new FaceItem();
            List<Point> markArray = faceDetResult.points;
            float keyPoints[][] = new float[markArray.size()][];
            for (int i = 0; i < markArray.size(); i++) {
                Point point = markArray.get(i);
                keyPoints[i] = new float[]{
                        point.x / (float) bitmapWidth,
                        point.y / (float) bitmapHeight,
                };
            }
            faceItem.markers = keyPoints;
            faceItem.boundaries = new float[]{
                    faceDetResult.rect.left / (float) bitmapWidth,
                    faceDetResult.rect.top / (float) bitmapHeight,
                    faceDetResult.rect.width() / (float) bitmapWidth,
                    faceDetResult.rect.height() / (float) bitmapHeight
            };
            faces.add(faceItem);

            faceFeatures.add(new co.polarr.renderer.entities.Context.FaceFeaturesState());
        }

        return faceStates;
    }

    // senstime 106 points to dlib 68 points
    private static FaceDetResult mapSenstimeToDlib(List<PointF> points) {
        FaceDetResult faceDetResult = new FaceDetResult();
        // face edge
        for (int i = 0; i <= 32; i += 2) {
            PointF srcPoint = points.get(i / 2);
            Point point = new Point(Math.round(srcPoint.x), Math.round(srcPoint.y));
            faceDetResult.points.add(point);
        }

        // eyebrows,nose,eyes
        for (int i = 33; i <= 63; i += 1) {
            PointF srcPoint = points.get(i);
            Point point = new Point(Math.round(srcPoint.x), Math.round(srcPoint.y));
            faceDetResult.points.add(point);
        }

        // mouth
        for (int i = 84; i <= 103; i += 1) {
            PointF srcPoint = points.get(i);
            Point point = new Point(Math.round(srcPoint.x), Math.round(srcPoint.y));
            faceDetResult.points.add(point);
        }

        faceDetResult.rect = getFaceRect(faceDetResult.points);
        return faceDetResult;
    }

    private static Rect getFaceRect(List<Point> points) {
        Rect rect = new Rect();

        rect.left = points.get(0).x;
        rect.bottom = points.get(8).y;
        rect.right = points.get(16).x;
        rect.top = centerPoint(points.get(19), points.get(24)).y;

        return rect;
    }

    private static Point centerPoint(Point... points) {
        Point center = new Point();
        int totalX = 0, totalY = 0;
        if (points != null) {
            for (Point point : points) {
                totalX += point.x;
                totalY += point.y;
            }

            center.x = totalX / points.length;
            center.y = totalY / points.length;
        }


        return center;
    }

    private static class FaceDetResult {
        List<Point> points = new ArrayList<>();
        Rect rect = new Rect();
    }
}
