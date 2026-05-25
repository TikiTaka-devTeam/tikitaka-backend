package com.tikitaka.backend.slide.service;

import com.tikitaka.backend.slide.dto.PdfSlideConvertResult;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfSlideConvertService {

    private static final int RENDER_DPI = 150;

    public PdfSlideConvertResult convertPdfToSlideImages(Path pdfPath, String documentFileKey) {
        try {
            if (pdfPath == null || !Files.exists(pdfPath)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PDF 파일을 찾을 수 없습니다.");
            }

            Path slideUploadDir = Paths.get(
                    System.getProperty("user.dir"),
                    "uploads",
                    "slides",
                    documentFileKey
            ).toAbsolutePath().normalize();

            Files.createDirectories(slideUploadDir);

            List<String> slideImageUrls = new ArrayList<>();

            File pdfFile = pdfPath.toFile();

            try (PDDocument pdfDocument = Loader.loadPDF(pdfFile)) {
                PDFRenderer renderer = new PDFRenderer(pdfDocument);
                int pageCount = pdfDocument.getNumberOfPages();

                for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                    BufferedImage image = renderer.renderImageWithDPI(pageIndex, RENDER_DPI);

                    int pageNumber = pageIndex + 1;
                    String imageFileName = "page_" + pageNumber + ".png";

                    Path imagePath = slideUploadDir.resolve(imageFileName).toAbsolutePath().normalize();

                    ImageIO.write(image, "png", imagePath.toFile());

                    String imageUrl = "/uploads/slides/" + documentFileKey + "/" + imageFileName;
                    slideImageUrls.add(imageUrl);
                }

                String thumbnailUrl = slideImageUrls.isEmpty()
                        ? "/uploads/materials/default-thumbnail.png"
                        : slideImageUrls.get(0);

                return new PdfSlideConvertResult(
                        slideImageUrls,
                        pageCount,
                        thumbnailUrl
                );
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PDF를 슬라이드 이미지로 변환하는 중 오류가 발생했습니다."
            );
        }
    }
}