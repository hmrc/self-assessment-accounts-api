
# self-assessment-accounts-api

`sbt "~run 9792"` or `sbt run` 

To view the RAML

Start api definition services

```
sm --start COMBINED_API_DEFINITION API_DEFINITION API_EXAMPLE_MICROSERVICE API_DOCUMENTATION_FRONTEND -f
sm --start ASSETS_FRONTEND -r 3.11.0 -f
```

Go to http://localhost:9680/api-documentation/docs/api/preview and enter http://localhost:9792/api/conf/1.0/application.raml 


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
