self-assessment-accounts-api
========================

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

The Self Assessment Accounts API allows a developer to:

- retrieve the overall liability broken down into overdue, payable and pending amounts
- retrieve a list of charges and payments for a given date range
- retrieve more detail about a specific transaction
- retrieve a list of charges made to an account for a given date range
- retrieve the history of changes to an individual charge
- retrieve a list of payments for a given date range
- retrieve the allocation details of a specific payment against one or more liabilities

## Requirements

- Scala 2.13.x
- Java 11
- sbt 1.7.x
- [Service Manager](https://github.com/hmrc/service-manager)

## Development Setup

Run the microservice from the console using: `sbt run` (starts on port 9792 by default)

Start the service manager profile: `sm --start MTDFB_ACCOUNTS`

## Run Tests

Run unit tests: `sbt test`

Run integration tests: `sbt it:test`

## Viewing Open API Spec (OAS) docs

To view documentation locally ensure the Self Assessment Accounts API is running, and run api-documentation-frontend:
`./run_local_with_dependencies.sh`
Then go to http://localhost:9680/api-documentation/docs/openapi/preview and use this port and version:
`http://localhost:9792/api/conf/2.0/application.yaml`

## Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog)

## Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)

## API Reference / Documentation

Available on
the [HMRC Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/self-assessment-accounts-api)

## License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
