{
	"url":"jdbc:mariadb://localhost/dev&useServerPrepStmts=true",
	"driverClassName":"org.mariadb.jdbc.Driver",
	"jmxEnabled":false,
	"testWhileIdle":true,
	"testOnBorrow":true,
	"validationQuery":"select min(report_status_id) from dynamic_report_status",
	"testOnReturn":false,
	"validationInterval":30000,
	"timeBetweenEvictionRunsMillis":30000,
	"maxActive":25,
	"initialSize":5,
	"maxWait":10000,
	"removeAbandonedTimeout":60,
	"minEvictableIdleTimeMillis":30000,
	"minIdle":2,
	"logAbandoned":false,
	"removeAbandoned":false
}