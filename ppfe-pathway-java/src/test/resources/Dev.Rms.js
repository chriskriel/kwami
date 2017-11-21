{
	"url":"jdbc:sqlmx://",
	"driverClassName":"com.tandem.sqlmx.SQLMXDriver",
	"dbProperties": {
		"catalog": "DEV",
		"schema": "RMS",
		"blobTableName": "DEV.RMS.BLOBS",
		"clobTableName": "DEV.RMS.CLOBS"
	},
	"testWhileIdle":false,
	"testOnBorrow":true,
	"validationQuery":"SELECT 1",
	"testOnReturn":false,
	"validationInterval":30000,
	"timeBetweenEvictionRunsMillis":30000,
	"maxActive":100,
	"initialSize":10,
	"maxWait":10000,
	"removeAbandonedTimeout":60,
	"minEvictableIdleTimeMillis":30000,
	"minIdle":2,
	"logAbandoned":true,
    "removeAbandoned":true
}
