# 工作台
## 需求分析和设计
- 工作台就是数据运营的看板,并且可以提供快捷操作入口,可以有效提高商家的工作效率
- 展示的数据：
  - 今日订单
  - 订单管理
  - 菜品总览
  - 套餐总览
  - 订单信息
- 简单的 CURD, 直接导入数据
- 接口设计: 都是 GET 请求
## Apache POI使用
- 介绍:
  - 是一个处理Office各种文件格式的开源项目,简单来说,就可以可以使用 POI在java程序中对Office各种文件进行读写操作
- 应用场景:
  - 银行网银导出交易明细数据
  - 各种业务系统导出 Excel报表
  - 批量导入业务数据
- 入门程序介绍:
```java
    @Test
    public void testWrite() throws IOException {
        // 在内存中创建一个 Excel 文件
        XSSFWorkbook sheets = new XSSFWorkbook();
        // 在 Excel文件中创建一个 sheet 页
        XSSFSheet sheet = sheets.createSheet("sheet-1");
        // 在 sheet 页中创建行对象
        XSSFRow row = sheet.createRow(1);  // 创建一行
        // 在行上创建单元格
        row.createCell(1).setCellValue("姓名");   // 表示在 第二个单元格写入内容
        row.createCell(2).setCellValue("成绩");

        row = sheet.createRow(2);
        row.createCell(1).setCellValue("张三");
        row.createCell(2).setCellValue("98");
        // 通过输出流写入到磁盘中
        FileOutputStream fs = new FileOutputStream(new File("hello.xlsx"));
        // 写入到磁盘中
        sheets.write(fs);
        fs.close();
        sheets.close();
    }

    /**
     *  从已有的 excel文件中读取内容
     */
    @Test
    public void testRead() throws Exception{
        FileInputStream fs = new FileInputStream(new File("hello.xlsx"));
        XSSFWorkbook excel = new XSSFWorkbook(fs);
        // 把文件中的文本内容读出来
        // 读取第一个 sheet 页
        XSSFSheet sheet = excel.getSheet("sheet-1");  // 或者这届根据索引获取到 sheet 页
        // 获取行号
        int lastRowNum = sheet.getLastRowNum();   // 注意行号从 0 开始
        // 开始遍历
        for(int i = 1; i <= lastRowNum ; i ++){
            // 开始读取
            XSSFRow row = sheet.getRow(i);
            // 遍历列
            String cellValue = row.getCell(1).getStringCellValue();
            String cellValue1 = row.getCell(2).getStringCellValue();
            System.out.println(cellValue+"   "+cellValue1);
        }
        // 关闭资源
        excel.close();
        fs.close();
    }
```
# 导入 运营数据到 Excel 报表
## 需求分析和设计
- 业务规则:
  - 导入Excel形式的报表文件
  - 导入近30天的运营数据
- 接口设计:
  - Path: /admin/report/export
- 当前接口没有返回数据,服务端通过输出流把 Excel文件写在到客户端浏览器
## 代码开发
- 实现步骤:
  - 设计excel模板文件
  - 查询近30天的运营数据
  - 将查询到的运营数据写入到模板文件
  - 通过输出流将 Excel 文件下载到客户端浏览器

    