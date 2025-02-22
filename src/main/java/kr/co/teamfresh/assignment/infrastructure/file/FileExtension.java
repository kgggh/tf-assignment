package kr.co.teamfresh.assignment.infrastructure.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum FileExtension {
    XLSX(List.of("xlsx", "xls"));

    private final List<String> extensions;

    public static FileExtension fromExtension(String extension) {
        return Arrays.stream(values())
            .filter(e -> e.extensions.contains(extension.toLowerCase()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("지원되지 않는 파일 확장자: " + extension));
    }
}
