package integration.toyprojects;

import integration.IntegrationBaseTest;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import refactoringml.db.Instance;
import refactoringml.db.RefactoringCommit;
import refactoringml.db.StableCommit;
import refactoringml.db.ProcessMetrics;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class R2ToyProjectTest extends IntegrationBaseTest {
	@Override
	protected String trackCommit(){return "bc15aee7cfaddde19ba6fefe0d12331fe98ddd46";}

	@Override
	protected String trackFileName(){return "Person.java";}

	@Override
	protected String getRepo() {
		return "https://github.com/jan-gerling/toyrepo-r2.git";
	}

	// This test helped us to realize (again) that when class name and file name don't match, we can't link the
	// refactoring. We opened a PR in RefactoringMiner; now it works!
	@Test
	public void refactorings() {
		List<RefactoringCommit> refactoringCommitList = getRefactoringCommits();
		Assert.assertEquals(10, refactoringCommitList.size());

		String renameCommit = "bc15aee7cfaddde19ba6fefe0d12331fe98ddd46";
		assertRefactoring(refactoringCommitList, renameCommit, "Rename Class", 1);

		RefactoringCommit renameRefactoring = refactoringCommitList.stream().filter(refactoringCommit ->
				refactoringCommit.getCommit().equals(renameCommit)).findFirst().get();

		String extractCommit = "515365875143aa84b5bbb5c3191e7654a942912f";
		assertRefactoring(refactoringCommitList, extractCommit, "Extract Class", 1);
		assertRefactoring(refactoringCommitList, extractCommit, "Move Attribute", 2);

		RefactoringCommit extractClassRefactoring = (RefactoringCommit) filterCommit(refactoringCommitList, extractCommit).stream()
				.filter(instance -> instance.getProcessMetrics().refactoringsInvolved == 6)
				.collect(Collectors.toList()).get(0);

		assertProcessMetrics(extractClassRefactoring, ProcessMetrics.toString(5, 40, 5, 1, 0, 1, 1.0, 0, 6));
		assertProcessMetrics(renameRefactoring, ProcessMetrics.toString(1, 5, 0, 1, 0, 1, 1.0, 0, 0));
	}

	@Test
	public void isSubclass() {
		List<RefactoringCommit> refactoringCommitList = getRefactoringCommits();
		Assert.assertEquals(10, refactoringCommitList.size());

		List<RefactoringCommit> areSubclasses = refactoringCommitList.stream().filter(refactoringCommit ->
				refactoringCommit.getClassMetrics().isInnerClass() &&
						refactoringCommit.getClassName().equals("org.apache.commons.cli.HelpFormatter.StringBufferComparator")).collect(Collectors.toList());
		List<RefactoringCommit> areNoSubclasses = refactoringCommitList.stream().filter(yes -> !yes.getClassMetrics().isInnerClass()).collect(Collectors.toList());

		Assert.assertEquals(0, areSubclasses.size());
		Assert.assertEquals(10, areNoSubclasses.size());
	}

	@Test
	public void stable() {
		// there are no instances of stable variables, as the repo is too small
		List<StableCommit> stableCommitList = getStableCommits();
		Assert.assertEquals(0, stableCommitList.size());
	}

	@Test
	public void commitMetaData(){
		String commit = "bc15aee7cfaddde19ba6fefe0d12331fe98ddd46";
		assertMetaDataRefactoring(
				commit,
				"rename class",
				"Rename Class\tPerson renamed to People",
				"@local/repos/toyrepo-r2/" + commit,
				"d56acf6b23d646528b4b04779b0fe64d74811052");
	}

	@Test
	public void projectMetrics() {
		assertProjectMetrics(4, 3, 1, 64, 56, 8);
	}
}