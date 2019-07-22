@onlyfilter
Feature: filter test

@onlyfilter1
Scenario: filter scenario 1
When Check 2
When System out "value out"
Then Verify 
|name|value|
|bla|vla|


Scenario: filter scenario 2
When Check 2
When System out "value out"
Then Verify 
|name|value|
|bla|vla|