package ee.kolbaska.kolbaska.controller;

import com.google.zxing.WriterException;
import ee.kolbaska.kolbaska.exception.CertificateNotFoundException;
import ee.kolbaska.kolbaska.request.AdminCertificateCreationRequest;
import ee.kolbaska.kolbaska.request.AdminCustomerUpdateRequest;
import ee.kolbaska.kolbaska.request.RestaurantRequest;
import ee.kolbaska.kolbaska.response.*;
import ee.kolbaska.kolbaska.service.AdminRestaurantService;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.RoleNotFoundException;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("${api.basepath}/admin")
@RequiredArgsConstructor
public class AdminRestaurantController {

    private final AdminRestaurantService service;

    @PostMapping(value = "/restaurant", consumes = "multipart/form-data")
    public ResponseEntity<RestaurantTableResponse> createRestaurant(
            @NotNull @ModelAttribute RestaurantRequest request
    ) throws Exception {
        return ResponseEntity.ok(service.createRestaurant(request));
    }

    @GetMapping("/category")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(service.getCategories());
    }

    @GetMapping("/restaurant")
    public ResponseEntity<List<RestaurantTableResponse>> returnRestaurantList() {
        return ResponseEntity.ok(service.returnRestaurantList());
    }

    @GetMapping("/restaurant/{code}")
    public ResponseEntity<RestaurantResponse> returnRestaurant(
            @NotNull @PathVariable String code
    ) throws Exception {
        return ResponseEntity.ok(service.returnRestaurant(code));
    }

    @GetMapping("/restaurant/waiter/{id}")
    public ResponseEntity<CustomerInformationResponse> getWaiter(
            @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.getWaiter(id));
    }

    @PutMapping("/restaurant/waiter")
    public ResponseEntity<CustomerUpdateResponse> updateWaiter(
            @NotNull @RequestBody AdminCustomerUpdateRequest request
    ) throws RoleNotFoundException {
        return ResponseEntity.ok(service.updateWaiter(request));
    }

    @PostMapping("/certificate")
    public ResponseEntity<AdminCertificateCreationResponse> createCertificate(
            @NotNull @RequestBody AdminCertificateCreationRequest request
    ) throws RoleNotFoundException, MessagingException, TemplateException, IOException, WriterException {
        return ResponseEntity.ok(service.createCertificate(request));
    }

    @GetMapping("/certificate")
    public ResponseEntity<List<AdminCertificateResponse>> getCertificates() {
        return ResponseEntity.ok(service.getCertificates());
    }

    @GetMapping("/certificate/{id}")
    public ResponseEntity<AdminCertificateInformationResponse> getCertificate(
            @NotNull @PathVariable String id
    ) throws CertificateNotFoundException {
        return ResponseEntity.ok(service.getCertificate(id));
    }

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
        headers.set("filename", "image.png");

        Resource file = service.downloadFile(fileName, type);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.contentLength())
                .contentType(type.equals("photo") ? MediaType.IMAGE_PNG : MediaType.APPLICATION_PDF)
                .body(file);
    }
}
