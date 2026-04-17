from flask import jsonify
import json

from py2neo import Graph

import pandas as pd
from utils.config import SysConfig
CATEGORY_INDEX = {
    "Disease": 0,
    "Drug": 1,
    "Food": 2,
    "Check": 3,
    "Department": 4,
    "Producer": 5,
    "Symptom": 6
}

# 医疗知识图谱操作类
class Kgdb:
    def __init__(self, uri, user, password, name=None):
        # 初始化图数据库连接
        self.g = Graph(uri, auth=(user, password), name=name)

    def getMultipleNodeInformation(self, node_count):
        """
        获取多个节点的信息
        :param node_count: 要获取的节点数量
        :return: 包含多个节点信息的列表
        """
        node_info_list = []

        # 使用 Cypher 查询语句一次性获取多个节点的信息
        query = f"MATCH (n) RETURN n LIMIT {node_count}"
        nodes_data = self.g.run(query).data()

        for node_data in nodes_data:
            node = dict(node_data['n'])
            # node_id = node_data['n'].id
            # node_info = {'id': node_id, **node}
            node_info = { **node}

            if 'center' in node_info:
                x = node_info['center']['coordinates'][0]
                y = node_info['center']['coordinates'][1]
                crs = node_info['center']['crs']['name']
                node_info['center'] = f"X:{x}, Y:{y}, CRS:{crs}"
            node_info_list.append(node_info)

        return node_info_list



#这个有问题？
    def getOneNode(self, key, value, label=None):
        """
        获取一个节点
        :param key:
        :param value:
        :return:
        """
        if label is None:
            kgdata = self.g.run(
                f"match (n1) where n1.{key}='{value}' return n1.name as name, id(n1) as id, labels(n1)[0] as label").data()
        else:
            kgdata = self.g.run(
                f"match (n1:{label}) where n1.{key}='{value}' return n1.name as name, id(n1) as id, labels(n1)[0] as label").data()
        if kgdata:
            return {'id': str(kgdata[0]['id']), 'name': kgdata[0]['name'],
                    'category': CATEGORY_INDEX.get(kgdata[0]['label'], 'Unknown')}
        else:
            return {'id': 'Not Found', 'name': 'Not Found', 'category': 'Unknown'}

    def getNodeInformation(self, nodeid):
        """
        获取节点信息
        :param nodeid:
        :return:
        """
        nodeData = self.g.run(f"match (n) where id(n)={nodeid} return n").data()[0]
        nodeData = dict(nodeData['n'])
        nodeData['id'] = nodeid
        if 'center' in nodeData:
            x = nodeData['center']['coordinates'][0]
            y = nodeData['center']['coordinates'][1]
            crs = nodeData['center']['crs']['name']
            nodeData['center'] = f"X:{x},Y:{y},CRS:{crs}"
        return nodeData

    def getRelationshipInformation(self, relationId):
        """
        获取关系信息
        :param relationId:
        :return:
        """
        res = {}
        res['id'] = relationId
        relationData = self.g.run(f"match ()-[r]->() where id(r)={relationId} return r, type(r) as type_r").data()[0]
        res.update(dict(relationData)['r'])
        res['type'] = relationData['type_r']
        return res

    def nodeRelationshipNode(self, label):
        """
        根据label获取数据
        :param label: 节点标签
        :return: 包含节点和关系数据的列表
        """
        data = []
        links = []
        kgdata = self.g.run(
            f"MATCH p=(n1:{label})-[r]->(n2) RETURN n1.name AS h_name, id(n1) AS h_id, type(r) AS r, id(r) AS r_id, labels(n2)[0] AS label, n2.name AS name, id(n2) AS t_id").data()

        # 检查 kgdata 是否为空
        if kgdata:
            for index, value in enumerate(kgdata):
                data.append(
                    {'id': str(value['t_id']), 'name': value['name'], 'category': CATEGORY_INDEX[value['label']]})
                links.append({'source': str(value['h_id']), 'target': str(value['t_id']), 'value': value['r'],
                              'id': str(value['r_id'])})
            data.append({'id': str(kgdata[0]['h_id']), 'name': kgdata[0]['h_name'], 'category': CATEGORY_INDEX[label]})
        else:
            # 处理 kgdata 为空的情况
            print("No data found for the given label.")

        return [data, links]

    def nodeReNodeName(self, name):
        """
        根据name模糊查询数据，为了解决数据重复导致前端画图错误，加一个id字段(用neo4j中给节点的id)
        :param name:
        :return:
        """
        data = []
        links = []
        kgdata = self.g.run(
            f"match (n1)-[r]->(n2) where n1.name contains '{name}' return n1.name as name1, labels(n1)[0] as label1 ,"
            f"type(r) as r, id(r) as r_id, r.name as r_name, labels(n2)[0] as label, n2.name as name, n2.data as data, id(n1) as id_head, "
            f"id(n2) as id_tail").data()
        data.append({'id': str(kgdata[0]['id_head']), 'name': kgdata[0]['name1'],
                     'category': CATEGORY_INDEX[kgdata[0]['label1']]})  # id得用字符串，echarts需要
        for value in kgdata:
            if value['name']:
                data.append(
                    {'id': str(value['id_tail']), 'name': value['name'], 'category': CATEGORY_INDEX[value['label']]})
                links.append({'source': str(value['id_head']), 'target': str(value['id_tail']), 'value': value['r'],
                              'id': str(value['r_id'])})
            else:
                data.append({'id': str(value['id_tail']), 'name': str(round(value['data'], 2)),
                             'category': CATEGORY_INDEX[value['label']]})
                links.append(
                    {'source': str(value['id_head']), 'target': str(value['id_tail']), 'value': value['r_name'],
                     'id': str(value['r_id'])})
        return [data, links]

    def nodeReNodeId(self, id):
        """
        根据id查询下一级节点与关系
        :param id:
        :return:
        """
        data = []
        links = []
        kgdata = self.g.run(
            f"match (n1)-[r]->(n2) where id(n1)={id} return n1.name as name1, n1.data as data1, labels(n1)[0] as label1 ,"
            f"type(r) as r, id(r) as r_id, r.name as r_name, r.compare_dataset as compare_dataset, labels(n2)[0] as label, n2.name as name, n2.data as data, id(n1) as id_head, "
            f"id(n2) as id_tail").data()
        if kgdata[0]['name1']:
            data.append({'id': str(kgdata[0]['id_head']), 'name': kgdata[0]['name1'],
                         'category': CATEGORY_INDEX[kgdata[0]['label1']]})
        else:
            data.append({'id': str(kgdata[0]['id_head']), 'name': str(round(kgdata[0]['data1'], 2)),
                         'category': CATEGORY_INDEX[kgdata[0]['label1']]})
        for value in kgdata:
            if value['name'] and not value['data'] is None:
                data.append(
                    {'id': str(value['id_tail']), 'name': str(round(value['data'], 2)), 'des': value['name'],
                     'category': CATEGORY_INDEX[value['label']]})
                if value['r_name']:
                    links.append(
                        {'source': str(value['id_head']), 'target': str(value['id_tail']), 'value': value['r_name'],
                         'id': str(value['r_id'])})
                else:
                    links.append({'source': str(value['id_head']), 'target': str(value['id_tail']),
                                  'value': value['compare_dataset'], 'id': str(value['r_id'])})
            elif value['name']:
                data.append(
                    {'id': str(value['id_tail']), 'name': value['name'], 'category': CATEGORY_INDEX[value['label']]})
                if value['r_name']:
                    links.append(
                        {'source': str(value['id_head']), 'target': str(value['id_tail']), 'value': value['r_name'],
                         'id': str(value['r_id'])})
                else:
                    links.append({'source': str(value['id_head']), 'target': str(value['id_tail']), 'value': value['r'],
                                  'id': str(value['r_id'])})
            else:
                data.append({'id': str(value['id_tail']), 'name': str(round(value['data'], 2)),
                             'category': CATEGORY_INDEX[value['label']]})
                if value['r_name']:
                    links.append(
                        {'source': str(value['id_head']), 'target': str(value['id_tail']), 'value': value['r_name'],
                         'id': str(value['r_id'])})
                else:
                    links.append(
                        {'source': str(value['id_head']), 'target': str(value['id_tail']), 'value': value['r'],
                         'id': str(value['r_id'])})
        return [data, links]


    def nodeReNodeAlias(self, label, alias):
        """
        根据节点标签查询下一级节点与关系
        :param label: 节点标签
        :return: 包含节点和关系数据的列表
        """

        query = (
            f"MATCH p=(n1:{label.capitalize()})-[r]->(n2) "
            f"WHERE n1.name='{alias}' "
            "RETURN n1 AS start, n2 AS end, relationships(p) AS segments, length(p) AS length"
        )

        kgdata = self.g.run(query).data()

        return kgdata

    def runCypher(self, cypher):
        """
        cypher语句执行
        :param cypher:
        :return:
        """
        try:
            self.g.run(cypher)
            return {'msg': 200}
        except:
            return {'msg': 401}


    def addNode(self, label, attributes):
        """
        创建节点，并返回节点信息
        :param label: 节点的标签
        :param attributes: 字典，包含节点的属性
        :return:
        """
        # 将属性字典转换为Cypher属性字符串
        attributes_str = ', '.join([f'{key}: ${key}' for key in attributes.keys()])
        params = attributes  # 将属性字典作为参数传递给Cypher查询

        try:
            # 使用参数化查询以防止注入攻击
            query = f"CREATE (n:{label} {{{attributes_str}}}) RETURN n.name AS name, id(n) AS id, labels(n)[0] AS label"
            kgdata = self.g.run(query, **params).data()
            if kgdata:
                return {'id': str(kgdata[0]['id']), 'name': kgdata[0]['name'],
                        'category': CATEGORY_INDEX[kgdata[0]['label']], 'msg': 200}
            else:
                return {'msg': 404, 'error': 'Node creation failed'}
        except Exception as e:
            return {'msg': 401, 'error': str(e)}








    def matchNode(self, label, attribute):
        """
        查询节点
        :param label:
        :param attribute:
        :return:
        """
        data = []
        if attribute is None:
            attribute = ''
        try:
            kgdata = self.g.run(
                f"match(n1:{label}{{{attribute}}}) return n1.name as name, n1.data as data, id(n1) as id, labels(n1)[0] as label").data()
            for value in kgdata:
                if value['name'] and not value['data'] is None:
                    data.append(
                        {'id': str(value['id']), 'name': str(round(value['data'], 2)), 'des': value['name'],
                         'category': CATEGORY_INDEX[value['label']]})
                elif value['name']:
                    data.append(
                        {'id': str(value['id']), 'name': value['name'],
                         'category': CATEGORY_INDEX[value['label']]})
                else:
                    data.append({'id': str(value['id']), 'name': str(round(value['data'], 2)),
                                 'category': CATEGORY_INDEX[value['label']]})
            msg = f"成功查询{len(kgdata)}个节点"
            return [data, msg]
        except:
            msg = f"查询失败"
            return [data, msg]




    def addedge(self, hid, tid, rtype, attributes):
        """
        添加关系，并返回关系的详细信息
        :param hid: 起始节点ID
        :param tid: 目标节点ID
        :param rtype: 关系类型
        :param attributes: 关系属性，字典形式
        :return: 关系的详细信息
        """
        # 将属性字典转换为Cypher属性字符串，为每个属性创建一个参数绑定
        attributes_str = ', '.join([f'{key}: ${key}' for key in attributes.keys()])
        params = attributes  # 将属性字典作为参数传递给Cypher查询

        try:
            # 使用参数化查询以防止注入攻击
            query = f"MATCH (n1), (n2) WHERE id(n1) = {hid} AND id(n2) = {tid} CREATE (n1)-[r:{rtype} {{{attributes_str}}}]->(n2) RETURN n1, n2, r, id(r) as rid"
            result = self.g.run(query, **params).data()
            newQuery = f"""
            MATCH p=(n1)-[r:{rtype}]->(n2)
            WHERE id(n1) = {hid} AND id(n2) = {tid}
            RETURN p
            """
            newResult = self.g.run(newQuery).data()
            returnQuery = """
            MATCH p=(n:Disease)-[r]->(m)
            RETURN n AS start, m AS end, r AS segments, length(p) AS length
            LIMIT 20
            """
            returnResult = self.g.run(newQuery).data()



            return newResult


        except Exception as e:
            return {'msg': 500, 'error': str(e)}

    def setEdge(self, id, attribute):
        """
        修改边属性
        :param id:
        :param attribute:
        :return:
        """
        try:
            kgdata = self.g.run(
                f"match(n1)-[r]->(n2) where id(r)={id} set r+={{{attribute}}} return id(n1) as hid").data()
            return {'id': str(kgdata[0]['hid']), 'msg': 200}
        except:
            return {'msg': 401}

    def delEdge(self, id):
        """
        删除边
        :param id:
        :return:
        """
        try:
            kgdata = self.g.run(f"match(n1)-[r]->(n2) where id(r)={id} return r").data()
            if len(kgdata) > 0:
                self.g.run(f"match(n1)-[r]->(n2) where id(r)={id} delete r").data()
                return {'msg': 200}
            else:
                return {'msg': 401}
        except:
            return {'msg': 401}

    def matchEdge(self, rtype, attribute):
        """
        查询边
        :param rtype:
        :param attribute:
        :return:
        """
        data = []
        links = []
        dataSet = set()
        linksSet = set()
        if attribute is None:
            attribute = ''
        try:
            kgdata = self.g.run(
                f"match(n1)-[r:{rtype}{{{attribute}}}]->(n2) return n1.name as name1, n1.data as data1, labels(n1)[0] as label1 ,"
                f"type(r) as r, id(r) as r_id, r.name as r_name, r.compare_dataset as compare_dataset, labels(n2)[0] as label, n2.name as name, n2.data as data, id(n1) as id_head, "
                f"id(n2) as id_tail").data()
            for value in kgdata:
                if value['name1'] and not value['data1'] is None:
                    if not str(value['id_head']) in dataSet:
                        data.append(
                            {'id': str(value['id_head']), 'name': str(round(value['data1'], 2)), 'des': value['name1'],
                             'category': CATEGORY_INDEX[value['label1']]})
                        dataSet.add(str(value['id_head']))
                elif value['name1']:
                    if not str(value['id_head']) in dataSet:
                        data.append(
                            {'id': str(value['id_head']), 'name': value['name1'],
                             'category': CATEGORY_INDEX[value['label1']]})
                        dataSet.add(str(value['id_head']))
                else:
                    if not str(value['id_head']) in dataSet:
                        data.append({'id': str(value['id_head']), 'name': str(round(value['data1'], 2)),
                                     'category': CATEGORY_INDEX[value['label1']]})
                        dataSet.add(str(value['id_head']))

                if value['name'] and not value['data'] is None:
                    if not str(value['id_tail']) in dataSet:
                        data.append(
                            {'id': str(value['id_tail']), 'name': str(round(value['data'], 2)), 'des': value['name'],
                             'category': CATEGORY_INDEX[value['label']]})
                        dataSet.add(str(value['id_tail']))
                    if value['r_name']:
                        if not str(value['r_id']) in linksSet:
                            links.append(
                                {'source': str(value['id_head']), 'target': str(value['id_tail']),
                                 'value': value['r_name'],
                                 'id': str(value['r_id'])})
                            linksSet.add(str(value['r_id']))
                    else:
                        if not str(value['r_id']) in linksSet:
                            links.append({'source': str(value['id_head']), 'target': str(value['id_tail']),
                                          'value': value['compare_dataset'], 'id': str(value['r_id'])})
                            linksSet.add(str(value['r_id']))
                elif value['name']:
                    if not str(value['id_tail']) in dataSet:
                        data.append(
                            {'id': str(value['id_tail']), 'name': value['name'],
                             'category': CATEGORY_INDEX[value['label']]})
                        dataSet.add(str(value['id_tail']))
                    if value['r_name']:
                        if not str(value['r_id']) in linksSet:
                            links.append(
                                {'source': str(value['id_head']), 'target': str(value['id_tail']),
                                 'value': value['r_name'],
                                 'id': str(value['r_id'])})
                            linksSet.add(str(value['r_id']))
                    else:
                        if not str(value['r_id']) in linksSet:
                            links.append(
                                {'source': str(value['id_head']), 'target': str(value['id_tail']), 'value': value['r'],
                                 'id': str(value['r_id'])})
                            linksSet.add(str(value['r_id']))
                else:
                    if not str(value['id_tail']) in dataSet:
                        data.append({'id': str(value['id_tail']), 'name': str(round(value['data'], 2)),
                                     'category': CATEGORY_INDEX[value['label']]})
                        dataSet.add(str(value['id_tail']))
                    if value['r_name']:
                        if not str(value['r_id']) in linksSet:
                            links.append(
                                {'source': str(value['id_head']), 'target': str(value['id_tail']),
                                 'value': value['r_name'],
                                 'id': str(value['r_id'])})
                            linksSet.add(str(value['r_id']))
                    else:
                        if not str(value['r_id']) in linksSet:
                            links.append(
                                {'source': str(value['id_head']), 'target': str(value['id_tail']), 'value': value['r'],
                                 'id': str(value['r_id'])})
                            linksSet.add(str(value['r_id']))
            msg = f"成功查询{len(kgdata)}条关系"
            return [data, links, msg]
        except:
            msg = f"查询失败"
            return [data, links, msg]

    def matchEdgeNode(self, hlabel, hwhereAttribute, tlabel, twhereAttribute, rtype, rwhereAttribute):
        """
        联合查询
        :param hlabel:
        :param hwhereAttribute:
        :param tlabel:
        :param twhereAttribute:
        :param rtype:
        :param rwhereAttribute:
        :return:
        """
        data = []
        links = []
        dataSet = set()
        linksSet = set()
        if hlabel:
            hlabel = ':' + hlabel
        if tlabel:
            tlabel = ':' + tlabel
        if rtype:
            rtype = ':' + rtype
        try:
            kgdata = self.g.run(f"match(n1{hlabel}{{{hwhereAttribute}}})-[r{rtype}{{{rwhereAttribute}}}]->(n2{tlabel}"
                                f"{{{twhereAttribute}}}) return n1.name as name1, n1.data as data1, labels(n1)[0] as label1 ,"
                                f"type(r) as r, id(r) as r_id, r.name as r_name, r.compare_dataset as compare_dataset, "
                                f"labels(n2)[0] as label, n2.name as name, n2.data as data, id(n1) as id_head, id(n2) as id_tail").data()
            for value in kgdata:
                if value['name1'] and not value['data1'] is None:
                    if not str(value['id_head']) in dataSet:
                        data.append(
                            {'id': str(value['id_head']), 'name': str(round(value['data1'], 2)), 'des': value['name1'],
                             'category': CATEGORY_INDEX[value['label1']]})
                        dataSet.add(str(value['id_head']))
                elif value['name1']:
                    if not str(value['id_head']) in dataSet:
                        data.append(
                            {'id': str(value['id_head']), 'name': value['name1'],
                             'category': CATEGORY_INDEX[value['label1']]})
                        dataSet.add(str(value['id_head']))
                else:
                    if not str(value['id_head']) in dataSet:
                        data.append({'id': str(value['id_head']), 'name': str(round(value['data1'], 2)),
                                     'category': CATEGORY_INDEX[value['label1']]})
                        dataSet.add(str(value['id_head']))

                if value['name'] and not value['data'] is None:
                    if not str(value['id_tail']) in dataSet:
                        data.append(
                            {'id': str(value['id_tail']), 'name': str(round(value['data'], 2)), 'des': value['name'],
                             'category': CATEGORY_INDEX[value['label']]})
                        dataSet.add(str(value['id_tail']))
                    if value['r_name']:
                        if not str(value['r_id']) in linksSet:
                            links.append(
                                {'source': str(value['id_head']), 'target': str(value['id_tail']),
                                 'value': value['r_name'],
                                 'id': str(value['r_id'])})
                            linksSet.add(str(value['r_id']))
                    else:
                        if not str(value['r_id']) in linksSet:
                            links.append({'source': str(value['id_head']), 'target': str(value['id_tail']),
                                          'value': value['compare_dataset'], 'id': str(value['r_id'])})
                            linksSet.add(str(value['r_id']))
                elif value['name']:
                    if not str(value['id_tail']) in dataSet:
                        data.append(
                            {'id': str(value['id_tail']), 'name': value['name'],
                             'category': CATEGORY_INDEX[value['label']]})
                        dataSet.add(str(value['id_tail']))
                    if value['r_name']:
                        if not str(value['r_id']) in linksSet:
                            links.append(
                                {'source': str(value['id_head']), 'target': str(value['id_tail']),
                                 'value': value['r_name'],
                                 'id': str(value['r_id'])})
                            linksSet.add(str(value['r_id']))
                    else:
                        if not str(value['r_id']) in linksSet:
                            links.append(
                                {'source': str(value['id_head']), 'target': str(value['id_tail']), 'value': value['r'],
                                 'id': str(value['r_id'])})
                            linksSet.add(str(value['r_id']))
                else:
                    if not str(value['id_tail']) in dataSet:
                        data.append({'id': str(value['id_tail']), 'name': str(round(value['data'], 2)),
                                     'category': CATEGORY_INDEX[value['label']]})
                        dataSet.add(str(value['id_tail']))
                    if value['r_name']:
                        if not str(value['r_id']) in linksSet:
                            links.append(
                                {'source': str(value['id_head']), 'target': str(value['id_tail']),
                                 'value': value['r_name'],
                                 'id': str(value['r_id'])})
                            linksSet.add(str(value['r_id']))
                    else:
                        if not str(value['r_id']) in linksSet:
                            links.append(
                                {'source': str(value['id_head']), 'target': str(value['id_tail']), 'value': value['r'],
                                 'id': str(value['r_id'])})
                            linksSet.add(str(value['r_id']))
            msg = f"成功查询{len(kgdata)}条数据"
            return [data, links, msg]
        except:
            msg = f"查询失败"
            return [data, links, msg]

    # def get_disease_nodes_from_db(self):
    #     # 查询语句
    #     query = "MATCH p=(n:Disease)-[]->() RETURN p LIMIT 20"
    #     result =self.g.run(query)
    #     data = []
    #     for record in result:
    #         data.append(record["p"])
    #     return data


    # def get_disease_nodes_from_db(self):
    #     # 查询语句
    #     query = "MATCH p=(n:Disease)-[]->() RETURN p LIMIT 20"
    #     nodes_data = self.g.run(query).data()
    #
    #     # 提取节点数据
    #     nodes = [record['p'] for record in nodes_data]
    #
    #     # 将节点数据转换为JSON格式字符串
    #     json_data = json.dumps(nodes, ensure_ascii=False)


        # return json_data

    def remove_control_characters(self, s):
        # 去除字符串中的控制字符
        return "".join(ch for ch in s if ch.isprintable())

    def preprocess_json_file(self, file_path):
        # 读取 JSON 文件
        with open(file_path, 'r', encoding='utf-8') as file:
            # 读取文件内容并移除非法字符
            json_content = self.remove_control_characters(file.read())
            # 解析 JSON 内容
            json_obj = json.loads(json_content)
        return json_obj



    # def get_disease_nodes_from_db(self):
    #     # query = """
    #     #     MATCH p=(n:Disease)-[]->()
    #     #     RETURN p
    #     #     LIMIT 1
    #     # """
    #     # result = self.g.run(query).data()
    #     # # df = pd.DataFrame(result)
    #     # # 序列化查询结果为 JSON 格式的字符串
    #     # # serialized_result = self.serialize_results(result)
    #     # # 返回 JSON 格式数据给前端
    #     # # return serialized_result
    #     # df = result.to_data_frame()
    #     json_file_path = "/Users/zhangziyu/Downloads/records (1).json"
    #
    #     # 预处理 JSON 文件
    #     json_obj = self.preprocess_json_file(json_file_path)
    #     return json_obj

    def matchNodeName(self, name):
        """
             根据关键词查找路径
             :param keyword: 用于搜索节点名称的关键词
             :return: 返回包含该关键词的所有路径
             """
        try:

            # query = """
            # MATCH p=(n)-[r]->(m)
            # WHERE n.name CONTAINS $keyword
            # RETURN n AS start, m AS end, collect(r) AS segments, length(p) AS length
            # LIMIT 5
            # """
            query = """
            MATCH p=(n)-[r]->(m)
            WHERE n.name CONTAINS $keyword
            
            RETURN p
            LIMIT 5
            """
            # RETURN n AS start, properties(m) AS end, collect(properties(r)) AS segments, length(p) AS length

            # query = """
            # MATCH p=(n:Disease)-[r]->(m)
            # RETURN startNode(p) AS start, endNode(p) AS end, relationships(p) AS segments, length(p) AS length
            # LIMIT 20
            # """
            # print(result)
            # print(result)
            # 假设 result 是通过前面提到的 Graph.run().data() 方法获得的列表
            # return {'p': result}
            result = self.g.run(query, keyword=name).data()
            path_details = []
            for record in result:
                path = record["p"]
                path_info = {
                    "start": path.start_node,
                    "end": path.end_node,
                    "segments": [rel for rel in path.relationships],
                    "length": len(path.relationships)
                }
                path_details.append(path_info)
            return path_details


            # # 将结果包装成可以被JSON序列化的形式
            # wrapped_result = []
            # for item in result:
            #     wrapped_item = {
            #         'start': properties(item['start']),
            #         'end': properties(item['end']),
            #         'segments': item['segments'],  # 已经是字典列表形式
            #         'length': item['length']
            #     }
            #     wrapped_result.append({'p': wrapped_item})
            #
            # return wrapped_result


            # wrapped_result = [{'p': item} for item in result]
            #
            # # 返回处理后的结果
            # return wrapped_result
            # return {'p': result}

        except Exception as e:
            return {'msg': 401, 'error': str(e)}



# # 创建一个实例
instance = Kgdb("http://localhost:7474", SysConfig.NEO4J_USER, SysConfig.NEO4J_PASSWORD, name="neo4j")


