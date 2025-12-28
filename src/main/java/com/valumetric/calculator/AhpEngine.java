package com.valumetric.calculator;

/**
 * AHP(Analytic Hierarchy Process) 엔진
 * 
 * Thomas L. Saaty가 개발한 계층화 분석법(AHP) 알고리즘 구현
 * 
 * <pre>
 * 【AHP 개요】
 * - 의사결정 문제를 계층 구조로 분해
 * - 쌍대비교(Pairwise Comparison)를 통해 상대적 중요도 측정
 * - 고유벡터(Eigenvector) 방법으로 가중치 도출
 * 
 * 【Saaty의 쌍대비교 척도 (1-9)】
 * 1: 동등 (Equal importance)
 * 3: 약간 중요 (Moderate importance)
 * 5: 중요 (Strong importance)
 * 7: 매우 중요 (Very strong importance)
 * 9: 절대 중요 (Extreme importance)
 * 2,4,6,8: 중간값 (Intermediate values)
 * 
 * 참고 문헌: "The Analytic Hierarchy Process" by Thomas L. Saaty (1980)
 * </pre>
 */
public class AhpEngine {

    // 일관성 비율(CR) 임계값: Saaty는 0.1(10%) 이하를 권장
    private static final double CR_THRESHOLD = 0.1;

    // Random Index (RI) 테이블: n=1~15에 대한 값 (Saaty의 연구 결과)
    // n=1,2는 항상 일관성이 있으므로 0
    private static final double[] RANDOM_INDEX = {
            0.00, // n=1
            0.00, // n=2
            0.58, // n=3
            0.90, // n=4
            1.12, // n=5
            1.24, // n=6
            1.32, // n=7
            1.41, // n=8
            1.45, // n=9
            1.49, // n=10
            1.51, // n=11
            1.48, // n=12
            1.56, // n=13
            1.57, // n=14
            1.59 // n=15
    };

    /**
     * AHP 계산 결과를 담는 클래스
     */
    public static class AhpResult {
        private final double[] weights; // 가중치 벡터
        private final double lambdaMax; // 최대 고유값 (λmax)
        private final double consistencyIndex; // 일관성 지수 (CI)
        private final double consistencyRatio; // 일관성 비율 (CR)
        private final boolean isConsistent; // 일관성 충족 여부

        public AhpResult(double[] weights, double lambdaMax,
                double consistencyIndex, double consistencyRatio,
                boolean isConsistent) {
            this.weights = weights;
            this.lambdaMax = lambdaMax;
            this.consistencyIndex = consistencyIndex;
            this.consistencyRatio = consistencyRatio;
            this.isConsistent = isConsistent;
        }

        public double[] getWeights() {
            return weights.clone();
        }

        public double getLambdaMax() {
            return lambdaMax;
        }

        public double getConsistencyIndex() {
            return consistencyIndex;
        }

        public double getConsistencyRatio() {
            return consistencyRatio;
        }

        public boolean isConsistent() {
            return isConsistent;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("AhpResult{\n");
            sb.append("  weights=[");
            for (int i = 0; i < weights.length; i++) {
                sb.append(String.format("%.4f", weights[i]));
                if (i < weights.length - 1)
                    sb.append(", ");
            }
            sb.append("]\n");
            sb.append(String.format("  λmax=%.4f\n", lambdaMax));
            sb.append(String.format("  CI=%.4f\n", consistencyIndex));
            sb.append(String.format("  CR=%.4f (%.2f%%)\n", consistencyRatio, consistencyRatio * 100));
            sb.append(String.format("  일관성: %s\n", isConsistent ? "충족 ✓" : "미충족 ✗"));
            sb.append("}");
            return sb.toString();
        }
    }

    /**
     * 쌍대비교 행렬에서 가중치 벡터를 계산합니다.
     * 
     * <pre>
     * 【알고리즘 (기하평균법 - Geometric Mean Method)】
     * 
     * Step 1: 각 행의 기하평균 계산
     *   GM_i = (a_i1 × a_i2 × ... × a_in)^(1/n)
     * 
     * Step 2: 정규화하여 가중치 도출
     *   w_i = GM_i / Σ(GM_j)
     * 
     * Step 3: 일관성 검증
     *   λmax = Σ((A×w)_i / w_i) / n
     *   CI = (λmax - n) / (n - 1)
     *   CR = CI / RI
     *   
     *   CR ≤ 0.1 이면 일관성 충족
     * </pre>
     * 
     * @param matrix n×n 쌍대비교 행렬 (양의 역수 행렬)
     * @return AhpResult 가중치 및 일관성 지표
     * @throws IllegalArgumentException 유효하지 않은 행렬인 경우
     */
    public AhpResult calculate(double[][] matrix) {
        validateMatrix(matrix);

        int n = matrix.length;

        // Step 1 & 2: 기하평균법으로 가중치 계산
        double[] weights = calculateWeightsByGeometricMean(matrix);

        // Step 3: 일관성 검증
        double lambdaMax = calculateLambdaMax(matrix, weights);
        double ci = calculateConsistencyIndex(lambdaMax, n);
        double cr = calculateConsistencyRatio(ci, n);
        boolean isConsistent = cr <= CR_THRESHOLD || n <= 2;

        return new AhpResult(weights, lambdaMax, ci, cr, isConsistent);
    }

    /**
     * 기하평균법(Geometric Mean Method)을 사용하여 가중치를 계산합니다.
     * 
     * <pre>
     * 【장점】
     * - Saaty가 권장하는 방법
     * - 순위 역전 문제가 발생하지 않음
     * - 계산이 간단하고 직관적
     * 
     * 【공식】
     * 1. 각 행(row)의 모든 요소를 곱함
     * 2. n제곱근을 취함 (n = 행렬 크기)
     * 3. 모든 기하평균의 합으로 나누어 정규화
     * </pre>
     */
    private double[] calculateWeightsByGeometricMean(double[][] matrix) {
        int n = matrix.length;
        double[] geometricMeans = new double[n];
        double sumOfGM = 0.0;

        // 각 행의 기하평균 계산
        for (int i = 0; i < n; i++) {
            double product = 1.0;
            for (int j = 0; j < n; j++) {
                product *= matrix[i][j];
            }
            geometricMeans[i] = Math.pow(product, 1.0 / n);
            sumOfGM += geometricMeans[i];
        }

        // 정규화하여 가중치 도출
        double[] weights = new double[n];
        for (int i = 0; i < n; i++) {
            weights[i] = geometricMeans[i] / sumOfGM;
        }

        return weights;
    }

    /**
     * 열 정규화 방법(Column Normalization)으로 가중치를 계산합니다.
     * (대안적 방법 - 참고용)
     * 
     * <pre>
     * 【공식】
     * 1. 각 열의 합계 계산
     * 2. 각 요소를 해당 열의 합계로 나눔 (정규화)
     * 3. 각 행의 평균을 구함 → 가중치
     * </pre>
     */
    public double[] calculateWeightsByColumnNormalization(double[][] matrix) {
        int n = matrix.length;

        // 열 합계 계산
        double[] columnSums = new double[n];
        for (int j = 0; j < n; j++) {
            columnSums[j] = 0.0;
            for (int i = 0; i < n; i++) {
                columnSums[j] += matrix[i][j];
            }
        }

        // 정규화된 행렬 생성 및 행 평균 계산
        double[] weights = new double[n];
        for (int i = 0; i < n; i++) {
            double rowSum = 0.0;
            for (int j = 0; j < n; j++) {
                rowSum += matrix[i][j] / columnSums[j];
            }
            weights[i] = rowSum / n;
        }

        return weights;
    }

    /**
     * 최대 고유값 (λmax) 계산
     * 
     * <pre>
     * 【공식】
     * λmax = (1/n) × Σ((A×w)_i / w_i)
     * 
     * 【설명】
     * - A: 쌍대비교 행렬
     * - w: 가중치 벡터
     * - 완벽히 일관된 행렬이면 λmax = n
     * </pre>
     */
    private double calculateLambdaMax(double[][] matrix, double[] weights) {
        int n = matrix.length;
        double lambdaMax = 0.0;

        // A × w 계산 (행렬-벡터 곱)
        double[] aw = new double[n];
        for (int i = 0; i < n; i++) {
            aw[i] = 0.0;
            for (int j = 0; j < n; j++) {
                aw[i] += matrix[i][j] * weights[j];
            }
        }

        // λmax = 평균((A×w)_i / w_i)
        for (int i = 0; i < n; i++) {
            if (weights[i] > 0) {
                lambdaMax += aw[i] / weights[i];
            }
        }
        lambdaMax /= n;

        return lambdaMax;
    }

    /**
     * 일관성 지수 (CI: Consistency Index) 계산
     * 
     * <pre>
     * 【공식】
     * CI = (λmax - n) / (n - 1)
     * 
     * 【해석】
     * - CI = 0: 완벽히 일관됨
     * - CI > 0: 일관성 오류 존재
     * </pre>
     */
    private double calculateConsistencyIndex(double lambdaMax, int n) {
        if (n <= 1) {
            return 0.0;
        }
        return (lambdaMax - n) / (n - 1);
    }

    /**
     * 일관성 비율 (CR: Consistency Ratio) 계산
     * 
     * <pre>
     * 【공식】
     * CR = CI / RI
     * 
     * 【해석】
     * - CR ≤ 0.1 (10%): 일관성 충족 ✓
     * - CR > 0.1: 일관성 미충족, 쌍대비교 재검토 필요 ✗
     * 
     * RI는 Random Index로, 무작위 행렬의 평균 CI 값
     * </pre>
     */
    private double calculateConsistencyRatio(double ci, int n) {
        if (n <= 2) {
            return 0.0; // n=1,2는 항상 일관성 있음
        }

        double ri = getRandomIndex(n);
        if (ri == 0) {
            return 0.0;
        }

        return ci / ri;
    }

    /**
     * Random Index (RI) 조회
     */
    private double getRandomIndex(int n) {
        if (n < 1 || n > RANDOM_INDEX.length) {
            // 15 초과 시 근사값 사용
            return 1.59;
        }
        return RANDOM_INDEX[n - 1];
    }

    /**
     * 쌍대비교 행렬 유효성 검증
     * 
     * <pre>
     * 【유효한 쌍대비교 행렬 조건】
     * 1. 정방행렬 (n × n)
     * 2. 모든 요소가 양수
     * 3. 대각선 요소는 1 (자기 자신과 비교 = 동등)
     * 4. 역수 관계: a_ij = 1 / a_ji
     * </pre>
     */
    private void validateMatrix(double[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            throw new IllegalArgumentException("행렬은 null이거나 비어있을 수 없습니다");
        }

        int n = matrix.length;

        for (int i = 0; i < n; i++) {
            if (matrix[i] == null || matrix[i].length != n) {
                throw new IllegalArgumentException("정방행렬(n×n)이어야 합니다");
            }

            for (int j = 0; j < n; j++) {
                // 양수 검증
                if (matrix[i][j] <= 0) {
                    throw new IllegalArgumentException(
                            String.format("모든 요소는 양수여야 합니다: matrix[%d][%d]=%.4f", i, j, matrix[i][j]));
                }

                // 대각선 요소 = 1 검증
                if (i == j && Math.abs(matrix[i][j] - 1.0) > 0.0001) {
                    throw new IllegalArgumentException(
                            String.format("대각선 요소는 1이어야 합니다: matrix[%d][%d]=%.4f", i, j, matrix[i][j]));
                }

                // 역수 관계 검증 (허용 오차: 0.0001)
                if (i != j) {
                    double expected = 1.0 / matrix[j][i];
                    if (Math.abs(matrix[i][j] - expected) > 0.0001) {
                        throw new IllegalArgumentException(
                                String.format("역수 관계 위반: matrix[%d][%d]=%.4f, 예상값=%.4f (1/matrix[%d][%d])",
                                        i, j, matrix[i][j], expected, j, i));
                    }
                }
            }
        }
    }

    /**
     * 유효한 쌍대비교 행렬을 생성하는 헬퍼 메서드
     * 상삼각 행렬 값만 제공하면 역수 관계를 자동으로 채워줍니다.
     * 
     * @param n             행렬 크기
     * @param upperTriangle 상삼각 행렬 값 (대각선 제외, 행 우선 순서)
     *                      예: n=3일 때 [a12, a13, a23]
     * @return 완성된 쌍대비교 행렬
     */
    public double[][] createPairwiseMatrix(int n, double... upperTriangle) {
        int expectedLength = n * (n - 1) / 2;
        if (upperTriangle.length != expectedLength) {
            throw new IllegalArgumentException(
                    String.format("상삼각 요소 개수가 맞지 않습니다. 예상: %d, 실제: %d",
                            expectedLength, upperTriangle.length));
        }

        double[][] matrix = new double[n][n];
        int idx = 0;

        for (int i = 0; i < n; i++) {
            matrix[i][i] = 1.0; // 대각선 = 1

            for (int j = i + 1; j < n; j++) {
                matrix[i][j] = upperTriangle[idx];
                matrix[j][i] = 1.0 / upperTriangle[idx]; // 역수 관계
                idx++;
            }
        }

        return matrix;
    }
}
