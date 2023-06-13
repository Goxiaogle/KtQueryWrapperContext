KtQueryWrapperContext - 让你的 MybatisPlus 条件语句查询更加优雅

## 在使用 MybatisPlus 封装条件查询语句的时候您是否会遇到这种情况： 

```kotlin
        val username = adminQueryVo.username
        val name = adminQueryVo.name
        val queryWrapper = KtQueryWrapper(Admin::class.java)
        if(StringUtils.isNotEmpty(username))
            queryWrapper.eq(Admin::username, username)
        if(StringUtils.isNotEmpty(name))
            queryWrapper.like(Admin::name, name)
        val result = baseMapper.selectPage(page, queryWrapper)
```

​	WTF?这 TM 要写死我，这还只有两个参数，你知道的，很多情况查询参数多的离谱。

## So，你应该直接使用咱们的小工具：KtQueryWrapperContext:

```kotlin
        val wrapperContext = KtQueryWrapperContext<Admin> {
            adminQueryVo.run {
                +::username value username run ::eq
                +::name value name run ::like
            }
        }
        val result = baseMapper.selectPage(page, wrapperContext)
```

​	只要用个`+`号便解决了一切，你也可以用：`+name property ::name run ::like`这种形式。当然，此处用了 run，如果你不喜欢用，你也可以：

```kotlin
        val wrapperContext = KtQueryWrapperContext<Admin> {
            +adminQueryVo::username value adminQueryVo.username run ::eq
            +adminQueryVo::name value adminQueryVo.name run ::like
        }
```

解释一下用法：
```kotlin
KtQueryWrapperContext<你的 POJO 类> {
	+你 POJO 类对应的字段 value 值 run 采用的查询方法
	+值 property 字段 run 采用的方法
}
// queryWrapper.eq(Admin::username, username) 转换效果如下：
+ Admin::username value username run ::eq
// 同时也可以这样：
+ username property Admin::username run ::eq
// run 后面的都是和 QueryWrapper 各种比较方法名字一样的，如 ::like, ::le, ::gt 等
```

​	有些人会注意到我一开始的示例代码中 `::username` 实际上引用的是 `AdminQueryVo` 的 username（`AdminQueryVo::username`），而并非是 Admin 的 username 字段。在实际开发中，我也推荐你使用 `Admin::username` ，而并非像我这样。

​	那会报错吗？实际上也并不会，而且运行结果还是对的。因为 MybatisPlus 底层就是获取这个字段的名称，而我的 QueryVo 类与 POJO 类的字段是一一对应的，字段名是相同的，所以我这里才会运行正常且正确。所以在非正式开发中，你只是单纯想写几个小项目练手，完全可以像我这样，QueryVo 和 POJO 字段名一一对应，然后直接 xxxQueryVo.run {}。

```kotlin
@Schema(description = "用户查询实体")
data class AdminQueryVo(
    @field:Schema(description = "用户名")
    val username: String?,
    @field:Schema(description = "昵称")
    val name: String?
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID = 1L
    }
}
```

```kotlin
@Data
@Schema(description = "用户")
@TableName("admin")
@EqualsAndHashCode(callSuper = true)
public class Admin extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "用户名")
	@TableField("username")
	private String username;

	@Schema(description = "密码")
	@TableField("password")
	private String password;

	@Schema(description = "昵称")
	@TableField("name")
	private String name;

	@Schema(description = "手机")
	@TableField("phone")
	private String phone;

	@Schema(description = "仓库id")
	@TableField("ware_id")
	private Long wareId;

	@Schema(description = "角色名称")
	@TableField(exist = false)
	private String roleName;
}
```

​	可以注意到，我 Admin 类是 Java 类，AdminQueryVo 是 Kotlin 类。需要注意的一点是，Kotlin 在 1.9 版本才支持引用 Java 类的字段，也就是说 Kt1.8 做不到：`+ Admin::username value username run ::eq`，但是由于我的 QueryVo 是 Kt 类，我又可以这样：` + AdminQueryVo::username value adminQueryVo.username run ::eq`，如果你 QueryVo 也是 Java 类，推荐直接一键转成 Kt 类，因为 QueryVo 相比 POJO 影响更小。

