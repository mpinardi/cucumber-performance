package steps;

import java.util.Random;

import org.junit.Assert;

//import cucumber.api.DataTable;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Steps {
	
	@When("^System out \"([^\"]*)\"$")
	public void system_out(String arg1) throws Exception {
		//System.out.println(arg1);
		java.util.Random rand = new Random();
		int sleep = (100+rand.nextInt(200)) + 1;
		Thread.sleep(sleep);
	}
	
	@When("^Check (\\d+)$")
	public void check(int arg1) throws Exception {
	   //System.out.println("int:" +arg1);

	}

	@Then("^Verify$")
	public void verify(DataTable arg1) throws Exception {
	  
	}
	
	@When("Fail me")
	public void fail_me() {
		Assert.fail();
	}

}
