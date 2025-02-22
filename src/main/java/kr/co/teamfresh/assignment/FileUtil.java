package kr.co.teamfresh.assignment;

public class FileUtil {
    public static String extractFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }

        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
