package edu.sungshin.ecopath

// Route 클래스: 경로 정보 모델
data class Route(
    val info: String,      // 경로 정보
    val distance: String,  // 거리
    val duration: String,  // 소요 시간
    val carType: String, // 교통 수단
)