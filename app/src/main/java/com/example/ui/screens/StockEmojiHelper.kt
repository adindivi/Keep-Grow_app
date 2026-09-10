package com.example.ui.screens

fun getStockEmoji(ticker: String, name: String): String {
    val upperTicker = ticker.uppercase()
    val upperName = name.uppercase()
    return when {
        upperTicker == "005930" || upperName.contains("삼성전자") -> "📱" // 갤럭시 스마트폰/가전제품
        upperTicker == "000660" || upperName.contains("SK하이닉스") || upperName.contains("하이닉스") -> "💾" // 메모리 반도체 칩
        upperTicker == "373220" || upperName.contains("LG에너지솔루션") || upperName.contains("에너지솔루션") -> "🔋" // 전기차 배터리
        upperTicker == "005380" || upperName.contains("현대자동차") || upperName.contains("현대차") -> "🚗" // 아이오닉/제네시스 자동차
        upperTicker == "035420" || upperName.contains("NAVER") || upperName.contains("네이버") -> "🔍" // 네이버 녹색 검색창
        upperTicker == "035720" || upperName.contains("카카오") && !upperName.contains("뱅크") -> "💬" // 카카오톡 메신저
        upperTicker == "323410" || upperName.contains("카카오뱅크") -> "💳" // 카카오프렌즈 체크카드
        upperTicker == "005490" || upperName.contains("POSCO") || upperName.contains("포스코") -> "🏭" // 포항제철소 철강제품
        upperTicker == "068270" || upperName.contains("셀트리온") -> "💊" // 바이오시밀러 의약품
        upperTicker == "000270" || upperName.contains("기아") -> "🚙" // 쏘렌토/EV9 자동차
        upperTicker == "207940" || upperName.contains("삼성바이오") -> "🔬" // 바이오 원제 위탁생산
        upperName.contains("금융") || upperName.contains("지주") || upperName.contains("은행") || upperName.contains("증권") || upperName.contains("화재") || upperName.contains("생명") -> "🏦" // 금융/은행 지점
        upperTicker == "051910" || upperName.contains("LG화학") -> "🧪" // 기초화학 및 전지 소재
        upperTicker == "006400" || upperName.contains("삼성SDI") -> "🔋" // 배터리 및 IT 소재
        upperTicker == "012330" || upperName.contains("현대모비스") -> "🔩" // 자동차 핵심 부품
        upperTicker == "028260" || upperName.contains("삼성물산") -> "🏢" // 래미안 아파트/건설
        upperTicker == "003670" || upperName.contains("포스코퓨처엠") || upperName.contains("에코프로") -> "🔋" // 양극재/배터리 소재
        upperTicker == "011200" || upperName.contains("HMM") -> "🚢" // 컨테이너선 화물 운송
        upperTicker == "096770" || upperName.contains("SK이노베이션") -> "🛢️" // 휘발유 및 석유 에너지 제품
        upperTicker == "033780" || upperName.contains("KT&G") -> "☕" // 정관장 홍삼 제품
        upperTicker == "003490" || upperName.contains("대한항공") -> "✈️" // 여객선 항공기 서비스
        upperTicker == "090430" || upperName.contains("아모레") -> "💄" // 설화수/헤라 화장품
        upperTicker == "017670" || upperTicker == "030200" || upperName.contains("텔레콤") || upperName.contains("KT") -> "📶" // 5G 요금제/휴대폰 통신
        upperTicker == "015760" || upperName.contains("한국전력") || upperName.contains("가스공사") -> "🔌" // 가정용 전력/가스 공급
        upperTicker == "010950" || upperName.contains("S-OIL") || upperName.contains("에스오일") -> "⛽" // 에스오일 주유소
        upperName.contains("제일제당") -> "🍚" // 햇반/비비고 식품 제품
        upperTicker == "035250" || upperName.contains("강원랜드") -> "🎰" // 슬롯머신 및 리조트
        upperTicker == "007070" || upperName.contains("GS리테일") -> "🏪" // GS25 편의점 매장
        upperTicker == "095720" || upperName.contains("맥쿼리") -> "🛣️" // 유료 백업 고속도로 및 터널
        upperName.contains("리츠") || upperName.contains("부동산") -> "🏢" // 오피스 빌딩 자산
        upperName.contains("KODEX") || upperName.contains("TIGER") -> "📈" // ETF 지수 투자 상품
        upperTicker == "017940" || upperName.contains("E1") -> "⛽" // 친환경 LPG 가스 충전소
        upperTicker == "000100" || upperName.contains("유한양행") -> "💊" // 안티푸라민 의약품
        upperTicker == "NVDA" || upperName.contains("NVIDIA") || upperName.contains("엔비디아") -> "🎮" // 지포스 그래픽카드(GPU)
        upperTicker == "AAPL" || upperName.contains("APPLE") || upperName.contains("애플") -> "🍏" // 아이폰/애플 기기
        upperTicker == "TSLA" || upperName.contains("TESLA") || upperName.contains("테슬라") -> "⚡" // 전기차/수퍼차저 충전
        else -> "⭐"
    }
}
