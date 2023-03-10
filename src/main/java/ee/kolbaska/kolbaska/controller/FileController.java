package ee.kolbaska.kolbaska.controller;

import ee.kolbaska.kolbaska.service.FileService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.basepath}/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService service;

    @GetMapping("/download/{fileName}/{type}")
    public ResponseEntity<Resource> downloadFile(
            @NotNull @PathVariable String fileName,
            @NotNull @PathVariable String type
    ) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s".formatted(fileName));
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        Resource file = service.downloadFile(fileName, type);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.contentLength())
                .contentType(type.equals("photo") ? MediaType.IMAGE_PNG : MediaType.APPLICATION_PDF)
                .body(file);
    }
}
