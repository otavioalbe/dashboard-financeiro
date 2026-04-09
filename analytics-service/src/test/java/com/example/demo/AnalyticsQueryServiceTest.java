package com.example.demo;

import com.example.demo.document.TransactionRecord;
import com.example.demo.dto.CategorySummaryResponse;
import com.example.demo.dto.FinancialSummaryResponse;
import com.example.demo.dto.TopExpenseResponse;
import com.example.demo.repository.TransactionRecordRepository;
import com.example.demo.service.AnalyticsQueryService;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsQueryService unit tests")
class AnalyticsQueryServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private TransactionRecordRepository repository;

    @InjectMocks
    private AnalyticsQueryService service;

    private static final String USER_ID = "otavio";

    @BeforeEach
    void setUp() {}

    // --- getSummary ---

    @Test
    @DisplayName("getSummary: deve somar créditos e débitos e calcular saldo líquido")
    void getSummary_returnsCorrectTotals() {
        Document creditDoc = new Document("_id", "CREDIT")
                .append("total", new Decimal128(new BigDecimal("1500.00")))
                .append("count", 3L);
        Document debitDoc = new Document("_id", "DEBIT")
                .append("total", new Decimal128(new BigDecimal("400.00")))
                .append("count", 2L);

        AggregationResults<Document> mockResults = mockAggregationResults(List.of(creditDoc, debitDoc));
        when(mongoTemplate.aggregate(any(Aggregation.class), anyString(), eq(Document.class)))
                .thenReturn(mockResults);

        FinancialSummaryResponse result = service.getSummary(USER_ID);

        assertThat(result.totalCredit()).isEqualByComparingTo("1500.00");
        assertThat(result.totalDebit()).isEqualByComparingTo("400.00");
        assertThat(result.netBalance()).isEqualByComparingTo("1100.00");
        assertThat(result.transactionCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getSummary: deve retornar zeros quando não há transações")
    void getSummary_returnsZeroWhenNoTransactions() {
        AggregationResults<Document> mockResults = mockAggregationResults(List.of());
        when(mongoTemplate.aggregate(any(Aggregation.class), anyString(), eq(Document.class)))
                .thenReturn(mockResults);

        FinancialSummaryResponse result = service.getSummary(USER_ID);

        assertThat(result.totalCredit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalDebit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.netBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.transactionCount()).isZero();
    }

    // --- getSummaryByCategory ---

    @Test
    @DisplayName("getSummaryByCategory: deve retornar lista de categorias com totais")
    void getSummaryByCategory_returnsGroupedResults() {
        Document foodDoc = new Document("_id", new Document("category", "FOOD").append("type", "DEBIT"))
                .append("total", new Decimal128(new BigDecimal("250.00")))
                .append("count", 4L);
        Document transportDoc = new Document("_id", new Document("category", "TRANSPORT").append("type", "DEBIT"))
                .append("total", new Decimal128(new BigDecimal("120.00")))
                .append("count", 2L);

        AggregationResults<Document> mockResults = mockAggregationResults(List.of(foodDoc, transportDoc));
        when(mongoTemplate.aggregate(any(Aggregation.class), anyString(), eq(Document.class)))
                .thenReturn(mockResults);

        List<CategorySummaryResponse> result = service.getSummaryByCategory(USER_ID);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().category()).isEqualTo("FOOD");
        assertThat(result.getFirst().type()).isEqualTo("DEBIT");
        assertThat(result.get(0).total()).isEqualByComparingTo("250.00");
        assertThat(result.get(0).count()).isEqualTo(4L);
        assertThat(result.get(1).category()).isEqualTo("TRANSPORT");
    }

    // --- getTopExpenses ---

    @Test
    @DisplayName("getTopExpenses: deve retornar top N despesas do usuário ordenadas por valor")
    void getTopExpenses_returnsMappedRecords() {
        TransactionRecord record = TransactionRecord.builder()
                .transactionId("tx-001")
                .userId(USER_ID)
                .category("FOOD")
                .description("Restaurante")
                .amount(new BigDecimal("350.00"))
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findByUserIdAndTypeOrderByAmountDesc(USER_ID, "DEBIT", PageRequest.of(0, 3)))
                .thenReturn(List.of(record));

        List<TopExpenseResponse> result = service.getTopExpenses(USER_ID, 3);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().transactionId()).isEqualTo("tx-001");
        assertThat(result.getFirst().amount()).isEqualByComparingTo("350.00");
        assertThat(result.getFirst().category()).isEqualTo("FOOD");
    }

    @Test
    @DisplayName("getTopExpenses: deve retornar lista vazia quando não há despesas")
    void getTopExpenses_returnsEmptyList() {
        when(repository.findByUserIdAndTypeOrderByAmountDesc(anyString(), anyString(), any()))
                .thenReturn(List.of());

        List<TopExpenseResponse> result = service.getTopExpenses(USER_ID, 5);

        assertThat(result).isEmpty();
        verify(repository).findByUserIdAndTypeOrderByAmountDesc(USER_ID, "DEBIT", PageRequest.of(0, 5));
    }

    @SuppressWarnings("unchecked")
    private AggregationResults<Document> mockAggregationResults(List<Document> docs) {
        AggregationResults<Document> mockResults = mock(AggregationResults.class);
        when(mockResults.getMappedResults()).thenReturn(docs);
        return mockResults;
    }
}
