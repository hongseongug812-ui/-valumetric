package com.valumetric.service;

import com.valumetric.calculator.AhpEngine;
import com.valumetric.document.SystemConfig;
import com.valumetric.dto.admin.AhpMatrixUpdateRequest;
import com.valumetric.dto.admin.AhpWeightResponse;
import com.valumetric.dto.admin.SalaryConfigUpdateRequest;
import com.valumetric.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 관리자 서비스 (MongoDB 버전)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final SystemConfigRepository configRepository;
    private final AhpEngine ahpEngine;

    /**
     * 급여 설정 조회
     */
    public SystemConfig getSalaryConfig() {
        return configRepository.getDefaultConfig();
    }

    /**
     * 급여 설정 수정
     */
    public SystemConfig updateSalaryConfig(SalaryConfigUpdateRequest request) {
        SystemConfig config = configRepository.getDefaultConfig();

        if (request.getFixedCostPerPerson() != null) {
            config.setFixedCostPerPerson(request.getFixedCostPerPerson());
        }
        if (request.getInsuranceRate() != null) {
            config.setInsuranceRate(request.getInsuranceRate());
        }
        if (request.getTargetProfitRate() != null) {
            config.setTargetProfitRate(request.getTargetProfitRate());
        }

        config.setUpdatedAt(LocalDateTime.now());
        return configRepository.save(config);
    }

    /**
     * 현재 AHP 가중치 조회
     */
    public AhpWeightResponse getCurrentAhpWeights() {
        SystemConfig config = configRepository.getDefaultConfig();

        List<SystemConfig.EvaluationCriteria> criteria = config.getEvaluationCriteria();
        String[] names = criteria.stream()
                .map(SystemConfig.EvaluationCriteria::getName)
                .toArray(String[]::new);
        double[] weights = config.getAhpWeights().stream()
                .mapToDouble(Double::doubleValue)
                .toArray();

        return AhpWeightResponse.builder()
                .criteriaNames(names)
                .weights(weights)
                .consistencyRatio(config.getConsistencyRatio() != null ? config.getConsistencyRatio() : 0.0)
                .isConsistent(config.getIsConsistent() != null ? config.getIsConsistent() : true)
                .message("현재 저장된 가중치")
                .build();
    }

    /**
     * AHP 쌍대비교 행렬로 가중치 계산 및 저장
     */
    public AhpWeightResponse calculateAndSaveAhpWeights(AhpMatrixUpdateRequest request) {
        int n = request.getMatrixSize();
        double[][] matrix = ahpEngine.createPairwiseMatrix(
                n, request.getUpperTriangleValues());

        AhpEngine.AhpResult result = ahpEngine.calculate(matrix);

        if (!result.isConsistent()) {
            log.warn("AHP 일관성 비율 초과: CR={}", result.getConsistencyRatio());
        }

        // 설정 저장
        SystemConfig config = configRepository.getDefaultConfig();

        // 가중치 업데이트
        List<Double> weightList = new ArrayList<>();
        for (double w : result.getWeights()) {
            weightList.add(w);
        }
        config.setAhpWeights(weightList);
        config.setConsistencyRatio(result.getConsistencyRatio());
        config.setIsConsistent(result.isConsistent());

        // 행렬 값 저장
        List<Double> matrixValues = new ArrayList<>();
        for (double v : request.getUpperTriangleValues()) {
            matrixValues.add(v);
        }
        config.setAhpMatrixValues(matrixValues);

        // 기준 이름 업데이트
        if (request.getCriteriaNames() != null && request.getCriteriaNames().length == n) {
            List<SystemConfig.EvaluationCriteria> criteriaList = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                criteriaList.add(SystemConfig.EvaluationCriteria.builder()
                        .name(request.getCriteriaNames()[i])
                        .weight(result.getWeights()[i])
                        .displayOrder(i + 1)
                        .isActive(true)
                        .build());
            }
            config.setEvaluationCriteria(criteriaList);
        }

        config.setUpdatedAt(LocalDateTime.now());
        configRepository.save(config);

        return AhpWeightResponse.builder()
                .weights(result.getWeights())
                .criteriaNames(request.getCriteriaNames())
                .lambdaMax(result.getLambdaMax())
                .consistencyIndex(result.getConsistencyIndex())
                .consistencyRatio(result.getConsistencyRatio())
                .isConsistent(result.isConsistent())
                .message(result.isConsistent() ? "계산 완료" : "⚠️ 일관성 비율 초과 (CR > 0.1)")
                .build();
    }

    /**
     * AHP 가중치 직접 설정
     */
    public AhpWeightResponse saveDirectWeights(String[] criteriaNames, double[] weights) {
        if (criteriaNames.length != weights.length) {
            throw new IllegalArgumentException("기준 이름과 가중치 개수가 일치하지 않습니다");
        }

        double sum = 0;
        for (double w : weights)
            sum += w;
        if (Math.abs(sum - 1.0) > 0.01) {
            throw new IllegalArgumentException("가중치 합계는 1이어야 합니다 (현재: " + sum + ")");
        }

        SystemConfig config = configRepository.getDefaultConfig();

        List<Double> weightList = new ArrayList<>();
        List<SystemConfig.EvaluationCriteria> criteriaList = new ArrayList<>();

        for (int i = 0; i < weights.length; i++) {
            weightList.add(weights[i]);
            criteriaList.add(SystemConfig.EvaluationCriteria.builder()
                    .name(criteriaNames[i])
                    .weight(weights[i])
                    .displayOrder(i + 1)
                    .isActive(true)
                    .build());
        }

        config.setAhpWeights(weightList);
        config.setEvaluationCriteria(criteriaList);
        config.setIsConsistent(true);
        config.setConsistencyRatio(0.0);
        config.setUpdatedAt(LocalDateTime.now());

        configRepository.save(config);

        return AhpWeightResponse.builder()
                .weights(weights)
                .criteriaNames(criteriaNames)
                .isConsistent(true)
                .consistencyRatio(0.0)
                .message("직접 설정 완료")
                .build();
    }
}
