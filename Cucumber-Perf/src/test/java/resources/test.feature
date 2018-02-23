Feature: test

@only
Scenario: scenario 1
When Check 2
When System out "value out"
Then Verify 
|name|value|
|bla|vla|

Scenario Outline: scenario 2
When System out "<value>"

Examples:
|value|
|test|
|fun|
|win|