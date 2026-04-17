
# from flask import Flask, jsonify, request
# from flask_cors import CORS
# from flask import g
#
# # 版本flask==2.0.3 flask_sqlalchemy==2.5.0
# from flask import request, jsonify, send_file
from flask import Flask, jsonify, request, g, send_file
from flask_cors import CORS

import kgcontrol
from chatbot import ChatBot
import kgcontrol_neo4j

# 初始化图数据库
kgdb = kgcontrol.Kgdb("neo4j://localhost:7687", SysConfig.NEO4J_USER, SysConfig.NEO4J_PASSWORD, name="neo4j")


uri = "bolt://localhost:7687"

username = SysConfig.NEO4J_USER
password = SysConfig.NEO4J_PASSWORD
# kgdbNeo4j = kgcontrol_neo4j.KgdbNeo4j(uri, username, password)


app = Flask(__name__)
CORS(app)  # 允许所有来源的跨域请求

# swagger = Swagger(app)
@app.before_request
def initialize_db():
    if 'neo4j_db' not in g:
        g.neo4j_db = kgcontrol_neo4j.KgdbNeo4j(uri, username, password)
@app.teardown_appcontext

def close_db(exception):
    neo4j_db = g.pop('neo4j_db', None)
    if neo4j_db is not None:
        neo4j_db.close()


# ----------------------------------------------------知识图谱显示页接口------------------------------------------------------

@app.route('/medical-knowledge-graph', methods=['GET'])
def get_medical_knowledge_graph():
    # 从 GET 请求参数中获取节点数量，默认为 200
    node_count = int(request.args.get('node_count', 200))
    # 假设你会在这里调用查询医疗知识图谱数据的函数，并传入节点数量参数
    medical_data = kgdb.getMultipleNodeInformation(node_count)

    data = []
    columns = []

    # 处理每个节点的信息
    for nodeInfo in medical_data:
        node_data = {}
        node_columns = []
        for key in nodeInfo:
            if key in NAME_DICT:
                node_data[key] = nodeInfo[key]
                node_columns.append({'label': NAME_DICT[key], 'prop': key})
        data.append(node_data)
        columns.append(node_columns)

    return jsonify(data=data, columns=columns, code=200)


@app.route("/getDiseaseNodes", methods=["GET"])
def get_disease_nodes():
    kgdbNeo4j = g.neo4j_db
    data = kgdbNeo4j.getDiseaseRelationships()

    return jsonify(data=data, code=200)

# ----------------------------------------------------知识管理页接口------------------------------------------------------
# 单节点获取接口
@app.route("/manage/kg/getOneNode", methods=["get"])
def getOneNode():
    label = request.args.get("label")
    key = request.args.get("key")
    value = request.args.get("value")
    data = kgdb.getOneNode(key, value, label)
    return jsonify(data=data, code=200)

NAME_DICT = {
    "name": "中文名称",
    "label": "中文标签",
    "count": "数量",
    "prevent": "预防措施",
    "cure_way": "治疗方法",
    "cause": "病因",
    "cure_lasttime": "治愈周期",
    "cured_prob": "治愈率",
    "cure_department": "治疗科室",
    "desc": "描述",
    "easy_get": "易感人群",
    "age": "年龄",
    "weight": "体重"
}

# 单点信息
@app.route("/manage/kg/getNodeInformation", methods=["get"])
def getNodeInformation():
    nodeid = request.args.get("nodeid")
    nodeInfo = kgdb.getNodeInformation(nodeid)
    data = {}
    column = []
    for key in nodeInfo:
        if key in NAME_DICT:
            data[key] = nodeInfo[key]
            column.append({'label': NAME_DICT[key], 'prop': key})
    return jsonify(data=data, column=column, code=200)

# 边信息
@app.route("/manage/kg/getRelationshipInformation", methods=["GET"])
def getRelationshipInformation():
    relationshipId = request.args.get("relationshipId")
    if not relationshipId:
        return jsonify(message="Missing 'relationshipId' parameter", code=400), 400

    relationshipInfo = kgdb.getRelationshipInformation(relationshipId)
    if not relationshipInfo:
        return jsonify(message="No information found for the given 'relationshipId'", code=404), 404

    data = {}
    column = []
    for key in relationshipInfo:
        if key in NAME_DICT:
            data[key] = relationshipInfo[key]
            column.append({'label': NAME_DICT[key], 'prop': key})
    return jsonify(data=data, column=column, code=200)


# cypher执行接口
@app.route("/manage/kg/runCypher", methods=["get"])
def runCypher():
    cypher = request.args.get("cypher")
    node = kgdb.runCypher(cypher)
    return jsonify(data=node, code=200)



@app.route('/addEdge', methods=['POST'])
def add_edge():
    data = request.json
    hid = data.get('hid')
    tid = data.get('tid')
    rtype = data.get('rtype')
    relationName = data.get('relationName')

    if not all([hid, tid, rtype, relationName]):
        return jsonify({'msg': 400, 'error': 'Missing required parameters'}), 400
    kgdbNeo4j = g.neo4j_db


    result = kgdbNeo4j.addEdge(hid, tid, rtype, relationName)
    return jsonify(result)



# 创建节点
@app.route("/manage/kg/createNode", methods=["POST"])
def createNode():
    data = request.json
    node_type = data.get("newNodeType")
    start_identity = data.get("start_identity")
    relation_type = data.get("relationType")
    relation_name = data.get("relationName")

    kgdbNeo4j = g.neo4j_db


    # 准备属性字典
    attributes = {key: data.get(key) for key in ["name", "cause", "cure_time", "cure_department", "possibility", "department"] if data.get(key) is not None}
    try:
        with kgdbNeo4j.g.session() as session:
            node_result = kgdbNeo4j.addNode(session, node_type, attributes)
            if node_result['msg'] != 200:
                raise Exception(node_result.get('error', 'Unknown error'))
            relationship = kgdbNeo4j.addEdge(session, start_identity, node_result['id'], relation_type, relation_name)

            if relationship['msg'] != 200:
                raise Exception(relationship.get('error', 'Unknown error'))
    except Exception as e:
        return jsonify({'msg': 'Failed to create relationship', 'error': str(e)}), 400
    # data_new = kgdbNeo4j.process_path_data([relationship['p']])
    # print(data_new)
    data_new = relationship.pop('p')

    # print(data_new)

    data = kgdbNeo4j.getDiseaseRelationships()
    data_all = data_new + data

    # return jsonify(data=relationship['p'], code=200)
    return jsonify(data=data_all, code=200)




@app.route("/manage/kg/setNode", methods=["POST"])
def setNode():
    id = request.json.get("id")
    attributes = request.json.get("attributes")  # Expecting a dictionary of attributes
    kgdbNeo4j = g.neo4j_db

    try:
        node_relationships = kgdbNeo4j.setNode(id, attributes)
        data_new = node_relationships.pop('result')
        data = kgdbNeo4j.getDiseaseRelationships()
        data_all = data_new + data
        return jsonify(data=data_all, code=200)
    except Exception as e:
        return jsonify(error=str(e), code=500)


# 删除节点接口
@app.route("/manage/kg/delNode", methods=["get"])
def delNode():
    id = request.args.get("id")
    kgdbNeo4j = g.neo4j_db

    node = kgdbNeo4j.delNode(id)
    data = node.get('data')

    # data = node.pop['data']
    return jsonify(data=data, code=200)


# 查询节点接口
@app.route("/manage/kg/matchNode", methods=["get"])
def matchNode():
    label = request.args.get("label")
    whereAttribute = request.args.get("whereAttribute")
    node, msg = kgdb.matchNode(label, whereAttribute)
    return jsonify(data=node, msg="msg", code=200)


# 创建边接口
@app.route("/manage/kg/createEdge", methods=["get"])
def createEdge():
    hid = request.args.get("hid")
    tid = request.args.get("tid")
    rtype = request.args.get("type")
    attribute = request.args.get("attribute")
    node = kgdb.addedge(hid, tid, rtype, attribute)
    return jsonify(data=node, code=200)


# 修改边接口
@app.route("/manage/kg/setEdge", methods=["get"])
def setEdge():
    id = request.args.get("id")
    attribute = request.args.get("attribute")
    node = kgdb.setEdge(id, attribute)
    return jsonify(data=node, code=200)


# 删除边接口
@app.route("/manage/kg/delEdge", methods=["get"])
def delEdge():
    id = request.args.get("id")
    node = kgdb.delEdge(id)
    return jsonify(data=node, code=200)


# 查询边接口
@app.route("/manage/kg/matchEdge", methods=["get"])
def matchEdge():
    rtype = request.args.get("type")
    whereAttribute = request.args.get("whereAttribute")
    data, links, msg = kgdb.matchEdge(rtype, whereAttribute)
    return jsonify(data=data, links=links, msg=msg, code=200)

# 联合查询边接口
@app.route("/manage/kg/matchEdgeNode", methods=["get"])
def matchEdgeNode():
    hlabel = request.args.get("hlabel")
    hwhereAttribute = request.args.get("hwhereAttribute")
    tlabel = request.args.get("tlabel")
    twhereAttribute = request.args.get("twhereAttribute")
    rtype = request.args.get("type")
    rwhereAttribute = request.args.get("rwhereAttribute")
    data, links, msg = kgdb.matchEdgeNode(hlabel, hwhereAttribute, tlabel, twhereAttribute, rtype, rwhereAttribute)
    return jsonify(data=data, links=links, msg=msg, code=200)


@app.route("/manage/kg/matchNodeName", methods=["get"])
def matchNodeName():
    name = request.args.get("name")
    kgdbNeo4j = g.neo4j_db
    data = kgdbNeo4j.matchNodeName(name)
    return jsonify(data=data, code=200)



# ----------------------------------------------------智能问答页接口------------------------------------------------------
# 智能问答-思知对话机器人接口
@app.route("/bot", methods=["get"])
def bot():
    chatbot = ChatBot()
    spoken = request.args.get("spoken")
    # 先用自己的机器人
    entities, answer = chatbot.answer(spoken)
    print(entities, answer)
    kgdbNeo4j = g.neo4j_db

    try:
        if len(entities):
            for entitiesName in entities:
                label = entities[entitiesName][0]
                data = kgdbNeo4j.nodeReNodeAlias(label, entitiesName)

            return jsonify(data=data, code=200)
    except Exception as e:
        # 定义一个默认的data变量
        data = {"error": str(e)}
        return jsonify(data=data, code=500)


if __name__ == "__main__":

    app.run(host='0.0.0.0', port=8080, debug=True)



