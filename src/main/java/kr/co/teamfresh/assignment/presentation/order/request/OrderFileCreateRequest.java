package kr.co.teamfresh.assignment.presentation.order.request;

import java.io.InputStream;

public record OrderFileCreateRequest(
    String originalFilename,
    String extension,
    InputStream content
) { }
