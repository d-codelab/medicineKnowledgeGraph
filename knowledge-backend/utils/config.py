#!/usr/bin/python
# -*- coding: UTF-8 -*-
from configparser import ConfigParser
from dotenv import load_dotenv
import os

load_dotenv()

class SysConfig(object):
    __doc__ = """ system config """

    # 单例，全局唯一
    def __new__(cls, *args, **kwargs):
        if not hasattr(SysConfig, '_instance'):
            SysConfig._instance = object.__new__(cls)
        return SysConfig._instance

    # 构建文件路径
    file_path = os.path.abspath("../data/config.ini")


    config_parser = ConfigParser()
    config_parser.read(file_path)

    NEO4J_HOST = "neo4j://localhost"
    NEO4J_PORT = 7687
    # 从环境变量获取
    NEO4J_USER = os.getenv("NEO4J_USER")
    NEO4J_PASSWORD = os.getenv("NEO4J_PASSWORD")

    current_directory = os.getcwd()

    # 目标文件的相对路径
    relative_path_to_data = "../data/medical.json"

    # 拼接成完整路径
    # DATA_ORIGIN_PATH = os.path.normpath(os.path.join(current_directory, relative_path_to_data))

    DATA_ORIGIN_PATH = "/Users/zhangziyu/Downloads/KGQAMedicine-main/data/medical.json"


    DATA_DICT_DIR = "../data/dict"

