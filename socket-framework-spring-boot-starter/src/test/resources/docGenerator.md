
aa
--
### 1.testObj（aa/testObj）

参数说明:

| 字段名称 |  字段类型   | 说明  | 是否必须 |
| ---- |:-------:| --- | ----:|
| a    | String  |     |      |
| b    | Integer |     |      |
| c    |  List   |     |      |
```Json
{
	"a":"xx",
	"b":123,
	"c":[
		"xx"
	]
}
```

返回值说明:

| 字段名称    |  字段类型   | 说明       | 是否必须 |
| ------- |:-------:| -------- | ----:|
| code    | Integer | 状态码      | true |
| message | String  | 成功或失败的信息 | true |
| data    |         | 数据       |      |
| data.a  | String  |          |      |
| data.b  | Integer |          |      |
| data.c  |  List   |          |      |
```Json
{
	"code":0,
	"data":{
		"a":"xx",
		"b":123,
		"c":[
			"xx"
		]
	},
	"message":"成功"
}
```

### 3.testBasicList（aa/testBasicList）

参数说明:

| 字段名称 |  字段类型  | 说明  | 是否必须 |
| ---- |:------:| --- | ----:|
| List | String |     |      |
```Json
[
	"xx"
]
```

返回值说明:

| 字段名称    |  字段类型   | 说明       | 是否必须 |
| ------- |:-------:| -------- | ----:|
| code    | Integer | 状态码      | true |
| message | String  | 成功或失败的信息 | true |
| data    |         | 数据       |      |
```Json
{
	"code":0,
	"data":[
		"xx"
	],
	"message":"成功"
}
```

### 3.testBasic（aa/testBasic）

接口说明:

**响应值是流媒体地址**

参数说明:

| 字段名称 | 字段类型 | 说明  | 是否必须 |
| ---- |:----:| --- | ----:|

```Json
112
```

返回值说明:

| 字段名称    |  字段类型   | 说明       | 是否必须 |
| ------- |:-------:| -------- | ----:|
| code    | Integer | 状态码      | true |
| message | String  | 成功或失败的信息 | true |
| data    | String  | 数据       |      |
```Json
{
	"code":0,
	"data":"xx",
	"message":"成功"
}
```

### 5.testResp（aa/testResp）

参数说明:
无

返回值说明:

| 字段名称    |  字段类型   | 说明       | 是否必须 |
| ------- |:-------:| -------- | ----:|
| code    | Integer | 状态码      | true |
| message | String  | 成功或失败的信息 | true |
| data    |         | 数据       |      |
```Json
{
	"code":0,
	"data":{},
	"message":"成功"
}
```

### 5.testVoidChannel（aa/testVoidChannel）

参数说明:
无

返回值说明:
无

### 5.testVoid（aa/testVoid）

参数说明:
无

返回值说明:
无

