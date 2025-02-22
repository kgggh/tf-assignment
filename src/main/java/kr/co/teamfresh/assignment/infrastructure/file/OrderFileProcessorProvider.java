package kr.co.teamfresh.assignment.infrastructure.file;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class OrderFileProcessorProvider {
    private final Map<FileExtension, OrderFileProcessor> processorMap;

    public OrderFileProcessorProvider(List<OrderFileProcessor> processors) {
        Map<FileExtension, OrderFileProcessor> map = new HashMap<>();

        for (OrderFileProcessor processor : processors) {
            FileExtension extension = processor.getExtension();
            map.put(extension, processor);
        }

        this.processorMap = map;
    }

    public OrderFileProcessor getProcessor(String extension) {
        FileExtension fileExtension = FileExtension.fromExtension(extension);

        return Optional.ofNullable(processorMap.get(fileExtension))
            .orElseThrow(() -> new RuntimeException("지원되지 않는 파일 타입: " + extension));
    }
}


