/* ===== Google Charts 로드 ===== */
google.charts.load('current', { packages: ['corechart', 'bar'] });
google.charts.setOnLoadCallback(() => {
  // 통계 탭 열릴 때 차트 로드
});

/* ===== 통계 탭 열릴 때 호출 ===== */
async function loadStats() {
  await loadDailyTrend();
  await loadMonthlyTrend();
  await loadPopularKeywords();
  await loadCategoryCount();
}

/* ===== 일별 검색 트렌드 ===== */
async function loadDailyTrend() {
  try {
    const res = await fetch('/api/stats/search-trend');
    const data = await res.json();

    const tableData = [['날짜', '검색 횟수']];
    data.forEach(item => {
      tableData.push([item.date, Number(item.count)]);
    });

    const chartData = google.visualization.arrayToDataTable(tableData);
    const options = {
      title: '',
      hAxis: { title: '날짜', textStyle: { fontSize: 11 } },
      vAxis: { title: '검색 횟수', minValue: 0 },
      legend: { position: 'none' },
      colors: ['#7c6ef5'],
      curveType: 'function',
      chartArea: { width: '80%', height: '70%' },
      backgroundColor: 'transparent'
    };

    const chart = new google.visualization.LineChart(
        document.getElementById('chart-daily-trend')
    );
    chart.draw(chartData, options);
  } catch (e) {
    document.getElementById('chart-daily-trend').innerHTML =
        '<p class="chart-empty">데이터가 없습니다.</p>';
  }
}

/* ===== 월별 검색 트렌드 ===== */
async function loadMonthlyTrend() {
  try {
    const res = await fetch('/api/stats/monthly-trend');
    const data = await res.json();

    const tableData = [['월', '검색 횟수']];
    data.forEach(item => {
      tableData.push([item.month, Number(item.count)]);
    });

    const chartData = google.visualization.arrayToDataTable(tableData);
    const options = {
      title: '',
      hAxis: { title: '월', textStyle: { fontSize: 11 } },
      vAxis: { title: '검색 횟수', minValue: 0 },
      legend: { position: 'none' },
      colors: ['#7c6ef5'],
      chartArea: { width: '80%', height: '70%' },
      backgroundColor: 'transparent'
    };

    const chart = new google.visualization.ColumnChart(
        document.getElementById('chart-monthly-trend')
    );
    chart.draw(chartData, options);
  } catch (e) {
    document.getElementById('chart-monthly-trend').innerHTML =
        '<p class="chart-empty">데이터가 없습니다.</p>';
  }
}

/* ===== 인기 검색어 TOP 10 ===== */
async function loadPopularKeywords() {
  try {
    const res = await fetch('/api/stats/popular-keywords');
    const data = await res.json();

    const tableData = [['검색어', '검색 횟수']];
    data.forEach(item => {
      tableData.push([item.keyword, Number(item.count)]);
    });

    const chartData = google.visualization.arrayToDataTable(tableData);
    const options = {
      title: '',
      hAxis: { title: '검색 횟수', minValue: 0 },
      vAxis: { title: '검색어', textStyle: { fontSize: 11 } },
      legend: { position: 'none' },
      colors: ['#e85d2d'],
      chartArea: { width: '70%', height: '75%' },
      backgroundColor: 'transparent'
    };

    const chart = new google.visualization.BarChart(
        document.getElementById('chart-popular-keywords')
    );
    chart.draw(chartData, options);
  } catch (e) {
    document.getElementById('chart-popular-keywords').innerHTML =
        '<p class="chart-empty">데이터가 없습니다.</p>';
  }
}

/* ===== 카테고리별 도서 수 ===== */
async function loadCategoryCount() {
  try {
    const res = await fetch('/api/stats/category-count');
    const data = await res.json();

    // 카테고리 코드 → 이름 변환
    const categoryNames = {
      '01': '국내도서', '02': '서양도서',
      '01-01': '국내>소설', '01-02': '국내>시/에세이',
      '01-03': '국내>인문', '01-04': '국내>IT/컴퓨터',
      '01-05': '국내>과학', '01-06': '국내>역사',
      '01-07': '국내>경제/경영', '01-08': '국내>어린이',
      '01-09': '국내>기타',
      '02-01': '서양>소설', '02-02': '서양>시/에세이'
    };

    const tableData = [['카테고리', '도서 수']];
    data.forEach(item => {
      const name = categoryNames[item.category] || item.category;
      tableData.push([name, Number(item.count)]);
    });

    const chartData = google.visualization.arrayToDataTable(tableData);
    const options = {
      title: '',
      legend: { position: 'right', textStyle: { fontSize: 11 } },
      chartArea: { width: '60%', height: '80%' },
      backgroundColor: 'transparent',
      colors: [
        '#7c6ef5', '#e85d2d', '#16a34a', '#0ea5e9',
        '#f59e0b', '#ec4899', '#8b5cf6', '#14b8a6',
        '#f97316', '#06b6d4', '#84cc16'
      ]
    };

    const chart = new google.visualization.PieChart(
        document.getElementById('chart-category-count')
    );
    chart.draw(chartData, options);
  } catch (e) {
    document.getElementById('chart-category-count').innerHTML =
        '<p class="chart-empty">데이터가 없습니다.</p>';
  }
}