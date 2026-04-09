package com.example.demo.service;

import com.example.demo.document.TransactionRecord;
import com.example.demo.dto.CategorySummaryResponse;
import com.example.demo.dto.FinancialSummaryResponse;
import com.example.demo.dto.MonthlySummaryResponse;
import com.example.demo.dto.TopExpenseResponse;
import com.example.demo.repository.TransactionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@RequiredArgsConstructor
public class AnalyticsQueryService implements IAnalyticsService {

    private static final String COLLECTION = "transaction_records";
    private static final String DEBIT = "DEBIT";
    private static final String CREDIT = "CREDIT";

    private final MongoTemplate mongoTemplate;
    private final TransactionRecordRepository repository;

    @Override
    public FinancialSummaryResponse getSummary(String userId) {
        Aggregation agg = newAggregation(
                match(Criteria.where("userId").is(userId)),
                group("type")
                        .sum("amount").as("total")
                        .count().as("count")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(agg, COLLECTION, Document.class);

        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal totalDebit = BigDecimal.ZERO;
        long transactionCount = 0;

        for (Document doc : results.getMappedResults()) {
            String type = doc.getString("_id");
            BigDecimal total = toBigDecimal(doc.get("total"));
            long count = toLong(doc.get("count"));
            transactionCount += count;
            if (CREDIT.equals(type)) totalCredit = total;
            else if (DEBIT.equals(type)) totalDebit = total;
        }

        return new FinancialSummaryResponse(totalCredit, totalDebit, totalCredit.subtract(totalDebit), transactionCount);
    }

    @Override
    public List<CategorySummaryResponse> getSummaryByCategory(String userId) {
        Aggregation agg = newAggregation(
                match(Criteria.where("userId").is(userId)),
                group(Fields.fields("category", "type"))
                        .sum("amount").as("total")
                        .count().as("count"),
                sort(org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Order.desc("total")
                ))
        );

        return mongoTemplate.aggregate(agg, COLLECTION, Document.class)
                .getMappedResults()
                .stream()
                .map(doc -> {
                    Document id = (Document) doc.get("_id");
                    return new CategorySummaryResponse(
                            id.getString("category"),
                            id.getString("type"),
                            toBigDecimal(doc.get("total")),
                            toLong(doc.get("count"))
                    );
                })
                .toList();
    }

    @Override
    public List<MonthlySummaryResponse> getMonthlySummary(String userId) {
        Aggregation agg = newAggregation(
                match(Criteria.where("userId").is(userId)),
                project("type", "amount")
                        .andExpression("year(createdAt)").as("year")
                        .andExpression("month(createdAt)").as("month"),
                group(Fields.fields("year", "month", "type"))
                        .sum("amount").as("total"),
                sort(org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Order.asc("_id.year"),
                        org.springframework.data.domain.Sort.Order.asc("_id.month")
                ))
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(agg, COLLECTION, Document.class);

        record MonthKey(int year, int month) {}
        Map<MonthKey, BigDecimal[]> map = new LinkedHashMap<>();

        for (Document doc : results.getMappedResults()) {
            Document id = (Document) doc.get("_id");
            int year = id.getInteger("year");
            int month = id.getInteger("month");
            String type = id.getString("type");
            BigDecimal total = toBigDecimal(doc.get("total"));
            MonthKey key = new MonthKey(year, month);
            map.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (CREDIT.equals(type)) map.get(key)[0] = total;
            else if (DEBIT.equals(type)) map.get(key)[1] = total;
        }

        return map.entrySet().stream()
                .map(e -> new MonthlySummaryResponse(
                        e.getKey().year(),
                        e.getKey().month(),
                        e.getValue()[0],
                        e.getValue()[1]
                ))
                .toList();
    }

    @Override
    public List<TopExpenseResponse> getTopExpenses(String userId, int limit) {
        return repository
                .findByUserIdAndTypeOrderByAmountDesc(userId, DEBIT, PageRequest.of(0, limit))
                .stream()
                .map(record -> new TopExpenseResponse(
                        record.getTransactionId(),
                        record.getCategory(),
                        record.getDescription(),
                        record.getAmount(),
                        record.getCreatedAt()
                ))
                .toList();
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof Decimal128 d) return d.bigDecimalValue();
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(value.toString());
    }
}
