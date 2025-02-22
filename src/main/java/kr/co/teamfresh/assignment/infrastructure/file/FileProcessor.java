package kr.co.teamfresh.assignment.infrastructure.file;

import java.io.InputStream;
import java.util.List;

public interface FileProcessor<T> {
    List<T> process(InputStream inputStream);
    FileExtension getExtension();
}
