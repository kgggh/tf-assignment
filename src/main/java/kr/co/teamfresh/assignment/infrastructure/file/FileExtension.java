package kr.co.teamfresh.assignment.infrastructure.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileExtension {
    XLSX("xlsx");

    private final String extension;
}
