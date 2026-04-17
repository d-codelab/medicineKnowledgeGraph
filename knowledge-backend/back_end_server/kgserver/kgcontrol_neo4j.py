import json

from neo4j import GraphDatabase
from utils.config import SysConfig
uri = "bolt://localhost:7687"
username = SysConfig.NEO4J_USER
password = SysConfig.NEO4J_PASSWORD

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
class KgdbNeo4j:
    def __init__(self, uri=uri, user=username, password=password):
        # Allow URI, username, and password to be passed as arguments, with defaults
        self.g = GraphDatabase.driver(uri, auth=(user, password))

    def close(self):
        self.g.close()




    def process_path_data(self, result):
        prefix = "4:d80b2f70-7d07-45e1-80d0-1cf92d5f28c2:"  # Static prefix for elementId
        path_details = []

        for record in result:
            path = record["p"]
            start_node = path.start_node
            end_node = path.end_node
            relationships = path.relationships

            path_info = {
                "start": {
                    "identity": start_node.element_id,
                    "labels": list(start_node.labels),
                    "properties": dict(start_node),
                    "elementId": f"{prefix}{start_node.element_id}"
                },
                "end": {
                    "identity": end_node.element_id,
                    "labels": list(end_node.labels),
                    "properties": dict(end_node),
                    "elementId": f"{prefix}{end_node.element_id}"
                },
                "segments": [
                    {
                        "start": {
                            "identity": rel.start_node.element_id,
                            "labels": list(rel.start_node.labels),
                            "properties": dict(rel.start_node),
                            "elementId": f"{prefix}{rel.start_node.element_id}"
                        },
                        "relationship": {
                            "identity": rel.element_id,
                            "start": rel.start_node.element_id,
                            "end": rel.end_node.element_id,
                            "type": rel.type,
                            "properties": dict(rel),
                            "elementId": f"{prefix}{rel.element_id}",
                            "startNodeElementId": f"{prefix}{rel.start_node.element_id}",
                            "endNodeElementId": f"{prefix}{rel.end_node.element_id}"
                        },
                        "end": {
                            "identity": rel.end_node.element_id,
                            "labels": list(rel.end_node.labels),
                            "properties": dict(rel.end_node),
                            "elementId": f"{prefix}{rel.end_node.element_id}"
                        }
                    }
                    for rel in relationships
                ],
                "length": len(relationships)
            }

            wrapped_path_info = {"p": path_info}
            path_details.append(wrapped_path_info)

        return path_details

    def matchNodeName(self, name):
        """
             根据关键词查找路径
             :param keyword: 用于搜索节点名称的关键词
             :return: 返回包含该关键词的所有路径
             """
        query = """
            MATCH p=(n)-[r]->(m)
            WHERE n.name CONTAINS $keyword OR m.name CONTAINS $keyword
            RETURN p
            LIMIT 10
        """
        with self.g.session() as session:
            result = session.run(query, keyword=name)
            path_details = self.process_path_data(result)
        return path_details

    def nodeReNodeAlias(self, label, alias):
        """
             根据节点标签查询下一级节点与关系
            :param label: 节点标签
            :return: 包含节点和关系数据的列表
        """
        query = (
            f"MATCH p=(n1:{label.capitalize()})-[r]->(n2) "
            f"WHERE n1.name=$alias "
            f"RETURN p "
            f"LIMIT 10"
        )
        # "RETURN n1 AS start, n2 AS end, relationships(p) AS segments, length(p) AS length"

        with self.g.session() as session:
            result = session.run(query,alias=alias)
            # session.run(query, alias=alias)

            path_details = self.process_path_data(result)
        return path_details


    def delNode(self, id):
        """
        删除节点并返回更新后的数据
        :param id: 节点的ID
        :return: 包含消息和数据的字典
        """
        try:
            with self.g.session() as session:
                # 检查节点是否存在

                # node_exists_query = session.run("MATCH (n1) WHERE id(n1) = $id RETURN n1", {"id": id}).data()
                node_exists_query = "MATCH (n1) WHERE id(n1) = {} RETURN n1".format(id)

                node_exists_result = session.run(node_exists_query).data()

                if len(node_exists_result) > 0:
                    # 删除节点的关系
                    delete_relationships_query = "MATCH (n1)-[r]-() WHERE id(n1) = {} DELETE r".format(id)
                    session.run(delete_relationships_query)

                    # 删除节点
                    delete_node_query = "MATCH (n1) WHERE id(n1) = {} DELETE n1".format(id)
                    session.run(delete_node_query)
                    # 获取更新后的部分数据
                    new_data_query = "MATCH p=(n:Disease)-[]->() RETURN p limit 20"
                    new_data = self.process_path_data(session.run(new_data_query))

                    return {'msg': 200, 'data': new_data}
                else:
                    return {'msg': 401, 'error': 'Node not found'}
        # 处理异常
        except Exception as e:
            return {'msg': 401, 'error': str(e)}



    def addEdge(self, session, hid, tid, rtype, relationName):
        try:
            query = f"""
                        MATCH (n1), (n2)
                        WHERE id(n1) = $hid AND id(n2) = $tid
                        CREATE p = (n1)-[r:{rtype} {{name: $relationship}}]->(n2)
                        RETURN p
                    """
            full_query = query.replace('$hid', str(hid)).replace('$tid', str(tid)).replace('$relationship', f'"{relationName}"')
            print(full_query)
            result = session.run(full_query)

            records = self.process_path_data(result)

            if records:
                relationship = records
                return {'p': relationship, 'msg': 200}

            else:
                return {'msg': 404, 'error': 'Relationship creation failed'}
        except Exception as e:
            print(f"Error creating relationship: {e}")
            return {'msg': 401, 'error': str(e)}




    def addNode(self, session, label, attributes):
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
                # 首先检查是否已经存在相同类型且名字相同的节点
                check_query = f"MATCH (n:{label} {{name: $name}}) RETURN n.name AS name, id(n) AS id, labels(n)[0] AS label"
                result = session.run(check_query, name=attributes['name'])
                record = result.single()

                if record:
                    return {
                        'id': str(record['id']),
                        'name': record['name'],
                        'category': CATEGORY_INDEX[record['label']],
                        'msg': 409,  # 409 Conflict
                        'error': 'Node already exists'
                    }

                # 使用参数化查询以防止注入攻击
                create_query = f"CREATE (n:{label} {{{attributes_str}}}) RETURN n.name AS name, id(n) AS id, labels(n)[0] AS label"
                result = session.run(create_query, **params)
                record = result.single()

                if record:
                    return {
                        'id': str(record['id']),
                        'name': record['name'],
                        'category': CATEGORY_INDEX[record['label']],
                        'msg': 200
                    }
                else:
                    return {'msg': 404, 'error': 'Node creation failed'}
        except Exception as e:
            return {'msg': 401, 'error': str(e)}



    def getDiseaseRelationships(self):
        query = """
            MATCH p=(n:Disease)-[]->()
            RETURN p
            LIMIT 20
        """
        try:
            with self.g.session() as session:
                result = session.run(query)
                data = self.process_path_data(result)
                return data
        except Exception as e:
            return {'msg': 401, 'error': str(e)}

    def setNode(self, id, attributes):
        """
        Modify node attributes.
        :param id: The Neo4j node ID to modify
        :param attributes: A dictionary of attributes to update on the node
        """

        def format_value(value):
            return json.dumps(value, ensure_ascii=False)

        formatted_query = (
                f"MATCH (n) WHERE id(n) = {id} "
                f"SET " + ", ".join([f"n.{key} = {format_value(value)}" for key, value in attributes.items()]) + " "
                                                                                                                 f"WITH n "
                                                                                                                 f"MATCH p=(n)-[r*1..2]-() "
                                                                                                                 f"RETURN p LIMIT 5"
        )

        with self.g.session() as session:
            try:
                result = session.run(formatted_query)
                processed_result = self.process_path_data(result)
                return {
                    'msg': 200,
                    'result': processed_result
                }
            except Exception as e:
                return {'msg': 401, 'error': str(e)}

















