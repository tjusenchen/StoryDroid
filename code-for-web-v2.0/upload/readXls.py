#!/usr/bin/python
# -*- coding: utf-8 -*-
# @Time    : 2021/12/1 17:21
# @File    : read_excel_test.py
import json
import os
import xlrd


def read_excel(excel_path, sheet_num=0):
    """
    :param excel_path:  xls/xlsx 路径
    :param sheet_num:   sheet下标，默认为0,即第一个sheet页
    :return:
    """
    # 判断文件是否文件
    resultsList = []
    if os.path.exists(excel_path):
        # 打开excel文件，获得句柄
        excel_handle = xlrd.open_workbook(excel_path)
        # 获取第一个工作表(就是excel底部的sheet)
        sheet = excel_handle.sheet_by_index(sheet_num)

        # nrows 返回该工作表有效行数
        for i in range(0, sheet.nrows):
            # 读取第i行数据，返回的是列表类型
            # print(sheet.row_values(i))
            resultsList.append(sheet.row_values(i))

        # print("====================================")
        #
        # # ncols 返回该工作表有效列数
        # for i in range(0, sheet.ncols):
        #     # 读取第i列数据，返回的是列表类型
        #     print(sheet.col_values(i))
    # else:
    #     raise FileNotFoundError("文件不存在")
    # print json.dumps(['str(tag)', 'apk_name'])
    # return json.dumps(['str(tag)', 'apk_name'])
    return json.dumps(resultsList)


if __name__ == '__main__':
    excel_data = read_excel(r"/home/zyx/Documents/log1101.xls")
    print(excel_data)
    # for i in excel_data:
    #     print i

# # 【打印结果】：
# ['编号', '姓名', '性别', '年龄', '籍贯']
# [1.0, '张三', '男', 15.0, '江苏南京']
# [2.0, '李四', '男', 20.0, '安徽合肥']
# [3.0, '王五', '男', 30.0, '广西桂林']
# [4.0, '陈真', '男', 15.0, '湖南长沙']
# [5.0, '扎哈', '女', 50.0, '广东深圳']
# == == == == == == == == == == == == == == == == == ==
# ['编号', 1.0, 2.0, 3.0, 4.0, 5.0]
# ['姓名', '张三', '李四', '王五', '陈真', '扎哈']
# ['性别', '男', '男', '男', '男', '女']
# ['年龄', 15.0, 20.0, 30.0, 15.0, 50.0]
# ['籍贯', '江苏南京', '安徽合肥', '广西桂林', '湖南长沙', '广东深圳']

