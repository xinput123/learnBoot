#!/bin/bash

# 批处理性能测试脚本

echo "=========================================="
echo "股票查询批处理性能测试"
echo "=========================================="
echo ""

BASE_URL="http://localhost:8080"

# 检查服务是否启动
echo "1. 检查服务状态..."
STATUS=$(curl -s "${BASE_URL}/stock/status" 2>/dev/null)
if [ "$STATUS" != "OK" ]; then
    echo "❌ 服务未启动或无法访问，请先启动 StockApplication"
    echo "   启动命令: mvn spring-boot:run"
    exit 1
fi
echo "✅ 服务正常运行"
echo ""

# 准备测试数据
STOCK_CODES="000001,000002,000003,000004,000005,000006,000007,000008,000009,000010"

echo "2. 测试不使用批处理..."
echo "   请求: GET ${BASE_URL}/stock/test/noBatch?codes=${STOCK_CODES}"
echo ""
RESULT_NO_BATCH=$(curl -s "${BASE_URL}/stock/test/noBatch?codes=${STOCK_CODES}")
echo "结果:"
echo "$RESULT_NO_BATCH" | python -m json.tool 2>/dev/null || echo "$RESULT_NO_BATCH"
echo ""

echo "=========================================="
echo ""

echo "3. 测试使用批处理..."
echo "   请求: GET ${BASE_URL}/stock/test/withBatch?codes=${STOCK_CODES}"
echo ""
RESULT_WITH_BATCH=$(curl -s "${BASE_URL}/stock/test/withBatch?codes=${STOCK_CODES}")
echo "结果:"
echo "$RESULT_WITH_BATCH" | python -m json.tool 2>/dev/null || echo "$RESULT_WITH_BATCH"
echo ""

echo "=========================================="
echo ""

echo "4. 性能对比测试..."
echo "   请求: GET ${BASE_URL}/stock/test/compare?codes=${STOCK_CODES}"
echo ""
RESULT_COMPARE=$(curl -s "${BASE_URL}/stock/test/compare?codes=${STOCK_CODES}")
echo "结果:"
echo "$RESULT_COMPARE" | python -m json.tool 2>/dev/null || echo "$RESULT_COMPARE"
echo ""

echo "=========================================="
echo "测试完成！"
echo "=========================================="
echo ""
echo "说明："
echo "- /stock/get/{code}      : 传统单个查询（不使用批处理）"
echo "- /stock/getBatch/{code} : 使用批处理的单个查询（推荐）"
echo ""
echo "批处理优势："
echo "✅ 多个并发请求合并为一次数据库查询"
echo "✅ 显著降低数据库压力"
echo "✅ 提高系统吞吐量"
echo "✅ 降低响应延迟"
echo ""

