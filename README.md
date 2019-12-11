## dbCheck 当前版本 1.0.68
### 功能
#### 一、索引检查
> - 1) 解析Mapper文件关联字段和条件字段未加索引提示
> - 2) 索引规范：唯一索引必须以uniq_ 开头、普通索引必须以 idx_ 开头 
> - 3) 条件字段增加函数提示  - 暂未加入

#### 二、复杂SQL分析
> - 1) 检测表关联查询超过三个的复杂SQL给出提示

#### 三、表中必加字段检查
> - 1) DBA规范中的必加字段 id,created_stime (datetime),modified_stime(datetime),is_del(tinyint)<br>
> - 2) created_stime (datetime),modified_stime(datetime) 默认值和modified_stime 修改更新 - 暂未加入

#### 四、检查与提示
> - 1) 统一叫 AutoDBCheck
> - 2) 提供改动检测和全量检测
> - 3) 快捷键和菜单方式
> - 4) 显示结果,有问题字段或语句下划线提示 - 暂时仅支持控制台显示

#### 五、使用方法
> - 1) 下载地址：[AutoDBCheck](https://github.com/AutohomeCorp/dbCheck/releases)
> - 2) 打开IDEA Settings->plugins-> install plugin from disk,导入jar 包后(install)，重启
> - 3) 配置数据库和项目信息：[配置说明](https://github.com/AutohomeCorp/dbCheck/wiki/配置说明)
> - 4) 自定义规则（自1.0.61版本支持）：[自定义规则说明](https://github.com/AutohomeCorp/dbCheck/wiki/自定义规则说明)

>   ![setting](https://files3.autoimg.cn/youche-h5/dbcheck/settings.png)
       
>   > 3.3、结果
       ![result](https://files3.autoimg.cn/youche-h5/dbcheck/result.png)

#### 六、规划
> - 1) 逐步支持DBA和能够检测的DB规范，实现工具化检查
> - 2) 提供Sql Server 数据库支持

#### 七、已知问题
> - 1) mybatis标签 ```<choost>``` 只能提取到第一个表达式内条件 - 正在优化中
