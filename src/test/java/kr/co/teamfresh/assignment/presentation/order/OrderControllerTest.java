package kr.co.teamfresh.assignment.presentation.order;

import kr.co.teamfresh.assignment.domain.product.Product;
import kr.co.teamfresh.assignment.domain.product.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(
    MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeAll
    void setup() {
        productRepository.saveAll(List.of(
            Product.register("사과", 100),
            Product.register("바나나", 100),
            Product.register("파인애플", 100)
        ));
    }

    @AfterEach
    void reset() {
        productRepository.findAll().forEach(product -> {
            product.changeStock(100);
            productRepository.save(product);
        });
        productRepository.flush();
    }

    @Test
    void 주문_생성_성공() throws Exception {
        //given
        var json = """
            {
              "ordererName": "김아무개",
              "address": "경기도 용인시 처인구 xxxx",
              "orderProducts": [
                {
                  "productId": "1",
                  "productName": "사과",
                  "quantity": 3
                }
              ]
            }
            """;
        //when
        //then

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated());
    }

    @Test
    void 재고가_부족할시_주문_생성에_실패한다() throws Exception {
        //given
        var json = """
            {
              "ordererName": "김아무개",
              "address": "경기도 용인시 처인구 xxxx",
              "orderProducts": [
                {
                  "productId": "1",
                  "productName": "사과",
                  "quantity": 999
                }
              ]
            }
            """;

        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("재고가 부족합니다.")));
    }

    @Test
    void 상품이_존재하지_않을시_주문_생성에_실패한다() throws Exception {
        //given
        var json = """
            {
              "ordererName": "김아무개",
              "address": "경기도 용인시 처인구 xxxx",
              "orderProducts": [
                {
                  "productId": 999,
                  "productName": "사과",
                  "quantity": 1
                }
              ]
            }
            """;

        // when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("해당 상품이 존재하지 않습니다")));
    }

    @Test
    void 엑셀파일로_주문시_양식과_데이터가_맞으면_주문이_생성_된다() throws Exception {
        //given
        var path = Paths.get("src/test/resources/excel/테스트_정상_주문서.xlsx");
        var fileContent = Files.readAllBytes(path);
        var file = new MockMultipartFile(
            "file",
            "테스트_미확인상품_주문서.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            fileContent
        );

        // 파일 업로드 요청 수행
        mockMvc.perform(multipart("/api/orders/import")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isCreated());
    }

    @Test
    void 엑셀파일로_주문시_상품이_존재하지_않을시_주문_생성에_실패한다() throws Exception {
        //given
        var path = Paths.get("src/test/resources/excel/테스트_미확인상품_주문서.xlsx");
        var fileContent = Files.readAllBytes(path);
        var file = new MockMultipartFile(
            "file",
            "테스트_미확인상품_주문서.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            fileContent
        );

        // 파일 업로드 요청 수행
        mockMvc.perform(multipart("/api/orders/import")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("해당 상품이 존재하지 않습니다")));
    }

    @Test
    void 엑셀파일로_주문시_상품의_재고가_부족할시_주문_생성에_실패한다() throws Exception {
        //given
        var path = Paths.get("src/test/resources/excel/테스트_재고부족_주문서.xlsx");
        var fileContent = Files.readAllBytes(path);
        var file = new MockMultipartFile(
            "file",
            "테스트_재고부족_주문서.xlsx.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            fileContent
        );

        // 파일 업로드 요청 수행
        mockMvc.perform(multipart("/api/orders/import")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("재고가 부족합니다.")));
    }

    @Test
    void 주문시_상품데이터가_중간에_존재하지않거나_재고가_부족할_경우_실패로_처리된다() throws Exception {
        //given
        var path = Paths.get("src/test/resources/excel/테스트_마지막_상품_재고부족_주문서.xlsx");
        var fileContent = Files.readAllBytes(path);
        var file = new MockMultipartFile(
            "file",
            "테스트_마지막_상품_재고부족_주문서.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            fileContent
        );

        // 파일 업로드 요청 수행
        mockMvc.perform(multipart("/api/orders/import")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("재고가 부족합니다.")));
    }

    @Test
    void 동시에_여러_주문들의_주문서_양식과_데이터가_알맞게_작성_되어있으면_주문을_정상적으로_처리하고_재고도_정상적으로_차감된다() throws Exception {
        // given
        var path = Paths.get("src/test/resources/excel/테스트_분산환경용_주문서.xlsx");
        var fileContent = Files.readAllBytes(path);
        var file = new MockMultipartFile(
            "file",
            "테스트_분산환경용_주문서.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            fileContent
        );

        var testThreadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(testThreadCount);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < testThreadCount; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    MvcResult result = mockMvc.perform(multipart("/api/orders/import")
                            .file(file)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                        .andExpect(status().isCreated())  // 기대값: 201 Created
                        .andReturn();
                    return result.getResponse().getStatus() == HttpStatus.CREATED.value();
                } catch (Exception e) {
                    System.err.println("요청 실패: " + e.getMessage());
                    return false;
                }
            }));
        }

        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        List<Boolean> results = new ArrayList<>();
        for (Future<Boolean> future : futures) {
            results.add(future.get());
        }

        long successCount = results.stream().filter(result -> result).count();
        long failCount = results.size() - successCount;

        System.out.println("성공한 요청 개수: " + successCount);
        System.out.println("실패한 요청 개수: " + failCount);

        assertThat(successCount).isEqualTo(testThreadCount);

        Product product = productRepository.findById(1L).get();

        assertThat(product.getStock()).isEqualTo(0);
    }

    @Test
    void 동시에_여러_주문들이_들어왔을때_일부_주문에_대한_상품_재고가_부족하면_나머지는_실패한다() throws Exception {
        // given
        var path = Paths.get("src/test/resources/excel/테스트_분산환경용_주문서.xlsx");
        var fileContent = Files.readAllBytes(path);
        var file = new MockMultipartFile(
            "file",
            "테스트_분산환경용_주문서.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            fileContent
        );

        //when
        var testThreadCount = 60;
        var executorService = Executors.newFixedThreadPool(testThreadCount);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < testThreadCount; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    var result = mockMvc.perform(multipart("/api/orders/import")
                            .file(file)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                        .andReturn();
                    return result.getResponse().getStatus() == HttpStatus.CREATED.value();
                } catch (Exception e) {
                    System.err.println("요청 실패: " + e.getMessage());
                    return false;
                }
            }));
        }

        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        List<Boolean> results = new ArrayList<>();
        for (Future<Boolean> future : futures) {
            results.add(future.get());
        }

        long successCount = results.stream().filter(result -> result).count();
        long failCount = results.size() - successCount;

        System.out.println("성공한 요청 개수: " + successCount);
        System.out.println("실패한 요청 개수: " + failCount);

        assertThat(successCount).isEqualTo(50);
        assertThat(failCount).isEqualTo(10);

        Product product = productRepository.findById(1L).get();

        assertThat(product.getStock()).isEqualTo(0);
    }
}
