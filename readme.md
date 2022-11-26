# 伟大的文件系统焯!!!

//下面 knife4j 生成的接口文档

# CrispFileSystem

**简介**:CrispFileSystem

**HOST**:localhost:8888

**联系人**:

**Version**:1.0

**接口路径**:/v2/api-docs

[TOC]

# CrispFileSystem

## 打开文件

**接口地址**:`/sys/cat`

**请求方式**:`POST`

**请求数据类型**:`application/json`

**响应数据类型**:`*/*`

**接口描述**:

**请求示例**:

```javascript
{
  "group": "",
  "path": "",
  "username": ""
}
```

**请求参数**:

| 参数名称             | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema |
| -------------------- | -------- | -------- | -------- | -------- | ------ |
| catDto               | catDto   | body     | true     | CatDto   | CatDto |
| &emsp;&emsp;group    |          |          | false    | string   |        |
| &emsp;&emsp;path     |          |          | false    | string   |        |
| &emsp;&emsp;username |          |          | false    | string   |        |

**响应状态**:

| 状态码 | 说明         | schema    |
| ------ | ------------ | --------- |
| 200    | OK           | R«string» |
| 201    | Created      |           |
| 401    | Unauthorized |           |
| 403    | Forbidden    |           |
| 404    | Not Found    |           |

**响应参数**:

| 参数名称 | 参数说明 | 类型           | schema         |
| -------- | -------- | -------------- | -------------- |
| code     |          | integer(int32) | integer(int32) |
| data     |          | string         |                |
| map      |          | object         |                |
| msg      |          | string         |                |

**响应示例**:

```javascript
{
	"code": 0,
	"data": "",
	"map": {},
	"msg": ""
}
```

## 改变目录

**接口地址**:`/sys/cd`

**请求方式**:`POST`

**请求数据类型**:`application/json`

**响应数据类型**:`*/*`

**接口描述**:

**请求示例**:

```javascript
{
  "group": "",
  "path": "",
  "username": ""
}
```

**请求参数**:

| 参数名称             | 参数说明      | 请求类型 | 是否必须 | 数据类型      | schema        |
| -------------------- | ------------- | -------- | -------- | ------------- | ------------- |
| changePathDto        | changePathDto | body     | true     | ChangePathDto | ChangePathDto |
| &emsp;&emsp;group    |               |          | false    | string        |               |
| &emsp;&emsp;path     |               |          | false    | string        |               |
| &emsp;&emsp;username |               |          | false    | string        |               |

**响应状态**:

| 状态码 | 说明         | schema    |
| ------ | ------------ | --------- |
| 200    | OK           | R«string» |
| 201    | Created      |           |
| 401    | Unauthorized |           |
| 403    | Forbidden    |           |
| 404    | Not Found    |           |

**响应参数**:

| 参数名称 | 参数说明 | 类型           | schema         |
| -------- | -------- | -------------- | -------------- |
| code     |          | integer(int32) | integer(int32) |
| data     |          | string         |                |
| map      |          | object         |                |
| msg      |          | string         |                |

**响应示例**:

```javascript
{
	"code": 0,
	"data": "",
	"map": {},
	"msg": ""
}
```

## 恢复文件系统

**接口地址**:`/sys/check`

**请求方式**:`POST`

**请求数据类型**:`application/json`

**响应数据类型**:`*/*`

**接口描述**:

**请求示例**:

```javascript
{
  "group": ""
}
```

**请求参数**:

| 参数名称          | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema   |
| ----------------- | -------- | -------- | -------- | -------- | -------- |
| groupDto          | groupDto | body     | true     | GroupDto | GroupDto |
| &emsp;&emsp;group |          |          | false    | string   |          |

**响应状态**:

| 状态码 | 说明         | schema    |
| ------ | ------------ | --------- |
| 200    | OK           | R«string» |
| 201    | Created      |           |
| 401    | Unauthorized |           |
| 403    | Forbidden    |           |
| 404    | Not Found    |           |

**响应参数**:

| 参数名称 | 参数说明 | 类型           | schema         |
| -------- | -------- | -------------- | -------------- |
| code     |          | integer(int32) | integer(int32) |
| data     |          | string         |                |
| map      |          | object         |                |
| msg      |          | string         |                |

**响应示例**:

```javascript
{
	"code": 0,
	"data": "",
	"map": {},
	"msg": ""
}
```

## 文件系统内部复制

**接口地址**:`/sys/copy`

**请求方式**:`POST`

**请求数据类型**:`application/json`

**响应数据类型**:`*/*`

**接口描述**:

**请求示例**:

```javascript
{
  "fromPath": "",
  "group": "",
  "toPath": "",
  "username": ""
}
```

**请求参数**:

| 参数名称             | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema  |
| -------------------- | -------- | -------- | -------- | -------- | ------- |
| copyDto              | copyDto  | body     | true     | CopyDto  | CopyDto |
| &emsp;&emsp;fromPath |          |          | false    | string   |         |
| &emsp;&emsp;group    |          |          | false    | string   |         |
| &emsp;&emsp;toPath   |          |          | false    | string   |         |
| &emsp;&emsp;username |          |          | false    | string   |         |

**响应状态**:

| 状态码 | 说明         | schema    |
| ------ | ------------ | --------- |
| 200    | OK           | R«string» |
| 201    | Created      |           |
| 401    | Unauthorized |           |
| 403    | Forbidden    |           |
| 404    | Not Found    |           |

**响应参数**:

| 参数名称 | 参数说明 | 类型           | schema         |
| -------- | -------- | -------------- | -------------- |
| code     |          | integer(int32) | integer(int32) |
| data     |          | string         |                |
| map      |          | object         |                |
| msg      |          | string         |                |

**响应示例**:

```javascript
{
	"code": 0,
	"data": "",
	"map": {},
	"msg": ""
}
```

## 删除文件

**接口地址**:`/sys/del`

**请求方式**:`POST`

**请求数据类型**:`application/json`

**响应数据类型**:`*/*`

**接口描述**:

**请求示例**:

```javascript
{
  "group": "",
  "path": "",
  "username": ""
}
```

**请求参数**:

| 参数名称             | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema |
| -------------------- | -------- | -------- | -------- | -------- | ------ |
| delDto               | delDto   | body     | true     | DelDto   | DelDto |
| &emsp;&emsp;group    |          |          | false    | string   |        |
| &emsp;&emsp;path     |          |          | false    | string   |        |
| &emsp;&emsp;username |          |          | false    | string   |        |

**响应状态**:

| 状态码 | 说明         | schema    |
| ------ | ------------ | --------- |
| 200    | OK           | R«string» |
| 201    | Created      |           |
| 401    | Unauthorized |           |
| 403    | Forbidden    |           |
| 404    | Not Found    |           |

**响应参数**:

| 参数名称 | 参数说明 | 类型           | schema         |
| -------- | -------- | -------------- | -------------- |
| code     |          | integer(int32) | integer(int32) |
| data     |          | string         |                |
| map      |          | object         |                |
| msg      |          | string         |                |

**响应示例**:

```javascript
{
	"code": 0,
	"data": "",
	"map": {},
	"msg": ""
}
```

## 查询目录内容

**接口地址**:`/sys/dir`

**请求方式**:`POST`

**请求数据类型**:`application/json`

**响应数据类型**:`*/*`

**接口描述**:

**请求示例**:

```javascript
{
  "group": "",
  "path": "",
  "username": ""
}
```

**请求参数**:

| 参数名称             | 参数说明      | 请求类型 | 是否必须 | 数据类型      | schema        |
| -------------------- | ------------- | -------- | -------- | ------------- | ------------- |
| changePathDto        | changePathDto | body     | true     | ChangePathDto | ChangePathDto |
| &emsp;&emsp;group    |               |          | false    | string        |               |
| &emsp;&emsp;path     |               |          | false    | string        |               |
| &emsp;&emsp;username |               |          | false    | string        |               |

**响应状态**:

| 状态码 | 说明         | schema          |
| ------ | ------------ | --------------- |
| 200    | OK           | R«List«FileVo»» |
| 201    | Created      |                 |
| 401    | Unauthorized |                 |
| 403    | Forbidden    |                 |
| 404    | Not Found    |                 |

**响应参数**:

| 参数名称               | 参数说明 | 类型              | schema         |
| ---------------------- | -------- | ----------------- | -------------- |
| code                   |          | integer(int32)    | integer(int32) |
| data                   |          | array             | FileVo         |
| &emsp;&emsp;address    |          | array             | integer        |
| &emsp;&emsp;createBy   |          | string            |                |
| &emsp;&emsp;createTime |          | string(date-time) |                |
| &emsp;&emsp;filename   |          | string            |                |
| &emsp;&emsp;id         |          | integer(int32)    |                |
| &emsp;&emsp;indirect   |          | array             | integer        |
| &emsp;&emsp;isDir      |          | integer(int32)    |                |
| &emsp;&emsp;length     |          | integer(int32)    |                |
| &emsp;&emsp;limit      |          | string            |                |
| map                    |          | object            |                |
| msg                    |          | string            |                |

**响应示例**:

```javascript
{
	"code": 0,
	"data": [
		{
			"address": [],
			"createBy": "",
			"createTime": "",
			"filename": "",
			"id": 0,
			"indirect": [],
			"isDir": 0,
			"length": 0,
			"limit": ""
		}
	],
	"map": {},
	"msg": ""
}
```

## 查询目录下所有子文件

**接口地址**:`/sys/dirs`

**请求方式**:`POST`

**请求数据类型**:`application/json`

**响应数据类型**:`*/*`

**接口描述**:

**请求示例**:

```javascript
{
  "group": "",
  "path": "",
  "username": ""
}
```

**请求参数**:

| 参数名称             | 参数说明      | 请求类型 | 是否必须 | 数据类型      | schema        |
| -------------------- | ------------- | -------- | -------- | ------------- | ------------- |
| changePathDto        | changePathDto | body     | true     | ChangePathDto | ChangePathDto |
| &emsp;&emsp;group    |               |          | false    | string        |               |
| &emsp;&emsp;path     |               |          | false    | string        |               |
| &emsp;&emsp;username |               |          | false    | string        |               |

**响应状态**:

| 状态码 | 说明         | schema          |
| ------ | ------------ | --------------- |
| 200    | OK           | R«List«FileVo»» |
| 201    | Created      |                 |
| 401    | Unauthorized |                 |
| 403    | Forbidden    |                 |
| 404    | Not Found    |                 |

**响应参数**:

| 参数名称               | 参数说明 | 类型              | schema         |
| ---------------------- | -------- | ----------------- | -------------- |
| code                   |          | integer(int32)    | integer(int32) |
| data                   |          | array             | FileVo         |
| &emsp;&emsp;address    |          | array             | integer        |
| &emsp;&emsp;createBy   |          | string            |                |
| &emsp;&emsp;createTime |          | string(date-time) |                |
| &emsp;&emsp;filename   |          | string            |                |
| &emsp;&emsp;id         |          | integer(int32)    |                |
| &emsp;&emsp;indirect   |          | array             | integer        |
| &emsp;&emsp;isDir      |          | integer(int32)    |                |
| &emsp;&emsp;length     |          | integer(int32)    |                |
| &emsp;&emsp;limit      |          | string            |                |
| map                    |          | object            |                |
| msg                    |          | string            |                |

**响应示例**:

```javascript
{
	"code": 0,
	"data": [
		{
			"address": [],
			"createBy": "",
			"createTime": "",
			"filename": "",
			"id": 0,
			"indirect": [],
			"isDir": 0,
			"length": 0,
			"limit": ""
		}
	],
	"map": {},
	"msg": ""
}
```

## 获取命令列表

**接口地址**:`/sys/help`

**请求方式**:`GET`

**请求数据类型**:`application/x-www-form-urlencoded`

**响应数据类型**:`*/*`

**接口描述**:

**请求参数**:

暂无

**响应状态**:

| 状态码 | 说明         | schema    |
| ------ | ------------ | --------- |
| 200    | OK           | R«HelpVo» |
| 401    | Unauthorized |           |
| 403    | Forbidden    |           |
| 404    | Not Found    |           |

**响应参数**:

| 参数名称                   | 参数说明 | 类型           | schema         |
| -------------------------- | -------- | -------------- | -------------- |
| code                       |          | integer(int32) | integer(int32) |
| data                       |          | HelpVo         | HelpVo         |
| &emsp;&emsp;cmddescription |          | array          | string         |
| &emsp;&emsp;cmdlist        |          | array          | string         |
| map                        |          | object         |                |
| msg                        |          | string         |                |

**响应示例**:

```javascript
{
	"code": 0,
	"data": {
		"cmddescription": [],
		"cmdlist": []
	},
	"map": {},
	"msg": ""
}
```

## 获取系统信息

**接口地址**:`/sys/info`

**请求方式**:`GET`

**请求数据类型**:`application/x-www-form-urlencoded`

**响应数据类型**:`*/*`

**接口描述**:

**请求参数**:

暂无

**响应状态**:

| 状态码 | 说明         | schema          |
| ------ | ------------ | --------------- |
| 200    | OK           | R«List«string»» |
| 401    | Unauthorized |                 |
| 403    | Forbidden    |                 |
| 404    | Not Found    |                 |

**响应参数**:

| 参数名称 | 参数说明 | 类型           | schema         |
| -------- | -------- | -------------- | -------------- |
| code     |          | integer(int32) | integer(int32) |
| data     |          | array          |                |
| map      |          | object         |                |
| msg      |          | string         |                |

**响应示例**:

```javascript
{
	"code": 0,
	"data": [],
	"map": {},
	"msg": ""
}
```

## 用户登录

**接口地址**:`/sys/login`

**请求方式**:`POST`

**请求数据类型**:`application/json`

**响应数据类型**:`*/*`

**接口描述**:

**请求示例**:

```javascript
{
  "password": "",
  "username": ""
}
```

**请求参数**:

| 参数名称             | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema   |
| -------------------- | -------- | -------- | -------- | -------- | -------- |
| loginDto             | loginDto | body     | true     | LoginDto | LoginDto |
| &emsp;&emsp;password |          |          | false    | string   |          |
| &emsp;&emsp;username |          |          | false    | string   |          |

**响应状态**:

| 状态码 | 说明         | schema    |
| ------ | ------------ | --------- |
| 200    | OK           | R«UserVo» |
| 201    | Created      |           |
| 401    | Unauthorized |           |
| 403    | Forbidden    |           |
| 404    | Not Found    |           |

**响应参数**:

| 参数名称             | 参数说明 | 类型           | schema         |
| -------------------- | -------- | -------------- | -------------- |
| code                 |          | integer(int32) | integer(int32) |
| data                 |          | UserVo         | UserVo         |
| &emsp;&emsp;group    |          | string         |                |
| &emsp;&emsp;username |          | string         |                |
| map                  |          | object         |                |
| msg                  |          | string         |                |

**响应示例**:

```javascript
{
	"code": 0,
	"data": {
		"group": "",
		"username": ""
	},
	"map": {},
	"msg": ""
}
```

## 创建目录

**接口地址**:`/sys/md`

**请求方式**:`POST`

**请求数据类型**:`application/json`

**响应数据类型**:`*/*`

**接口描述**:

**请求示例**:

```javascript
{
  "group": "",
  "path": "",
  "username": ""
}
```

**请求参数**:

| 参数名称             | 参数说明   | 请求类型 | 是否必须 | 数据类型   | schema     |
| -------------------- | ---------- | -------- | -------- | ---------- | ---------- |
| makeDirDto           | makeDirDto | body     | true     | MakeDirDto | MakeDirDto |
| &emsp;&emsp;group    |            |          | false    | string     |            |
| &emsp;&emsp;path     |            |          | false    | string     |            |
| &emsp;&emsp;username |            |          | false    | string     |            |

**响应状态**:

| 状态码 | 说明         | schema    |
| ------ | ------------ | --------- |
| 200    | OK           | R«string» |
| 201    | Created      |           |
| 401    | Unauthorized |           |
| 403    | Forbidden    |           |
| 404    | Not Found    |           |

**响应参数**:

| 参数名称 | 参数说明 | 类型           | schema         |
| -------- | -------- | -------------- | -------------- |
| code     |          | integer(int32) | integer(int32) |
| data     |          | string         |                |
| map      |          | object         |                |
| msg      |          | string         |                |

**响应示例**:

```javascript
{
	"code": 0,
	"data": "",
	"map": {},
	"msg": ""
}
```

## 创建文件

**接口地址**:`/sys/newfile`

**请求方式**:`POST`

**请求数据类型**:`application/json`

**响应数据类型**:`*/*`

**接口描述**:

**请求示例**:

```javascript
{
  "group": "",
  "path": "",
  "username": ""
}
```

**请求参数**:

| 参数名称             | 参数说明   | 请求类型 | 是否必须 | 数据类型   | schema     |
| -------------------- | ---------- | -------- | -------- | ---------- | ---------- |
| newFileDto           | newFileDto | body     | true     | NewFileDto | NewFileDto |
| &emsp;&emsp;group    |            |          | false    | string     |            |
| &emsp;&emsp;path     |            |          | false    | string     |            |
| &emsp;&emsp;username |            |          | false    | string     |            |

**响应状态**:

| 状态码 | 说明         | schema    |
| ------ | ------------ | --------- |
| 200    | OK           | R«string» |
| 201    | Created      |           |
| 401    | Unauthorized |           |
| 403    | Forbidden    |           |
| 404    | Not Found    |           |

**响应参数**:

| 参数名称 | 参数说明 | 类型           | schema         |
| -------- | -------- | -------------- | -------------- |
| code     |          | integer(int32) | integer(int32) |
| data     |          | string         |                |
| map      |          | object         |                |
| msg      |          | string         |                |

**响应示例**:

```javascript
{
	"code": 0,
	"data": "",
	"map": {},
	"msg": ""
}
```

## 删除目录

**接口地址**:`/sys/rd`

**请求方式**:`POST`

**请求数据类型**:`application/json`

**响应数据类型**:`*/*`

**接口描述**:

**请求示例**:

```javascript
{
  "group": "",
  "path": "",
  "username": ""
}
```

**请求参数**:

| 参数名称             | 参数说明     | 请求类型 | 是否必须 | 数据类型     | schema       |
| -------------------- | ------------ | -------- | -------- | ------------ | ------------ |
| removeDirDto         | removeDirDto | body     | true     | RemoveDirDto | RemoveDirDto |
| &emsp;&emsp;group    |              |          | false    | string       |              |
| &emsp;&emsp;path     |              |          | false    | string       |              |
| &emsp;&emsp;username |              |          | false    | string       |              |

**响应状态**:

| 状态码 | 说明         | schema |
| ------ | ------------ | ------ |
| 200    | OK           | R«int» |
| 201    | Created      |        |
| 401    | Unauthorized |        |
| 403    | Forbidden    |        |
| 404    | Not Found    |        |

**响应参数**:

| 参数名称 | 参数说明 | 类型           | schema         |
| -------- | -------- | -------------- | -------------- |
| code     |          | integer(int32) | integer(int32) |
| data     |          | integer(int32) | integer(int32) |
| map      |          | object         |                |
| msg      |          | string         |                |

**响应示例**:

```javascript
{
	"code": 0,
	"data": 0,
	"map": {},
	"msg": ""
}
```

## 保存文件系统

**接口地址**:`/sys/save`

**请求方式**:`POST`

**请求数据类型**:`application/json`

**响应数据类型**:`*/*`

**接口描述**:

**请求示例**:

```javascript
{
  "group": ""
}
```

**请求参数**:

| 参数名称          | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema   |
| ----------------- | -------- | -------- | -------- | -------- | -------- |
| groupDto          | groupDto | body     | true     | GroupDto | GroupDto |
| &emsp;&emsp;group |          |          | false    | string   |          |

**响应状态**:

| 状态码 | 说明         | schema    |
| ------ | ------------ | --------- |
| 200    | OK           | R«string» |
| 201    | Created      |           |
| 401    | Unauthorized |           |
| 403    | Forbidden    |           |
| 404    | Not Found    |           |

**响应参数**:

| 参数名称 | 参数说明 | 类型           | schema         |
| -------- | -------- | -------------- | -------------- |
| code     |          | integer(int32) | integer(int32) |
| data     |          | string         |                |
| map      |          | object         |                |
| msg      |          | string         |                |

**响应示例**:

```javascript
{
	"code": 0,
	"data": "",
	"map": {},
	"msg": ""
}
```

## host 文件系统与本地文件系统复制

**接口地址**:`/sys/simdisk`

**请求方式**:`POST`

**请求数据类型**:`application/json`

**响应数据类型**:`*/*`

**接口描述**:

**请求示例**:

```javascript
{
  "fromPath": "",
  "group": "",
  "toPath": "",
  "username": ""
}
```

**请求参数**:

| 参数名称             | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema  |
| -------------------- | -------- | -------- | -------- | -------- | ------- |
| copyDto              | copyDto  | body     | true     | CopyDto  | CopyDto |
| &emsp;&emsp;fromPath |          |          | false    | string   |         |
| &emsp;&emsp;group    |          |          | false    | string   |         |
| &emsp;&emsp;toPath   |          |          | false    | string   |         |
| &emsp;&emsp;username |          |          | false    | string   |         |

**响应状态**:

| 状态码 | 说明         | schema    |
| ------ | ------------ | --------- |
| 200    | OK           | R«string» |
| 201    | Created      |           |
| 401    | Unauthorized |           |
| 403    | Forbidden    |           |
| 404    | Not Found    |           |

**响应参数**:

| 参数名称 | 参数说明 | 类型           | schema         |
| -------- | -------- | -------------- | -------------- |
| code     |          | integer(int32) | integer(int32) |
| data     |          | string         |                |
| map      |          | object         |                |
| msg      |          | string         |                |

**响应示例**:

```javascript
{
	"code": 0,
	"data": "",
	"map": {},
	"msg": ""
}
```

## 写入文件

**接口地址**:`/sys/writeFile`

**请求方式**:`POST`

**请求数据类型**:`application/json`

**响应数据类型**:`*/*`

**接口描述**:

**请求示例**:

```javascript
{
  "data": "",
  "group": "",
  "path": "",
  "username": ""
}
```

**请求参数**:

| 参数名称             | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema   |
| -------------------- | -------- | -------- | -------- | -------- | -------- |
| writeDto             | writeDto | body     | true     | WriteDto | WriteDto |
| &emsp;&emsp;data     |          |          | false    | string   |          |
| &emsp;&emsp;group    |          |          | false    | string   |          |
| &emsp;&emsp;path     |          |          | false    | string   |          |
| &emsp;&emsp;username |          |          | false    | string   |          |

**响应状态**:

| 状态码 | 说明         | schema    |
| ------ | ------------ | --------- |
| 200    | OK           | R«string» |
| 201    | Created      |           |
| 401    | Unauthorized |           |
| 403    | Forbidden    |           |
| 404    | Not Found    |           |

**响应参数**:

| 参数名称 | 参数说明 | 类型           | schema         |
| -------- | -------- | -------------- | -------------- |
| code     |          | integer(int32) | integer(int32) |
| data     |          | string         |                |
| map      |          | object         |                |
| msg      |          | string         |                |

**响应示例**:

```javascript
{
	"code": 0,
	"data": "",
	"map": {},
	"msg": ""
}
```
