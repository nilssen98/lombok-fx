
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.PostGeneratedConstructor;

//@RequiredArgsConstructor
@AllArgsConstructor
class PostGeneratedConstructorTest {
	public PostGeneratedConstructorTest() {
		int i = 0;
		
		switch (i) {
		case 1: return;
		
		default:
			break;
		}
		
		if (true) return;
		for (;;) return;
		for (;;) { return; }
		while(true) return;
		while(true) { return; }
	}
	
	private String name;
	private final Integer start = 0;
	private final Integer end = 0;
	
	@PostGeneratedConstructor
	private void validate() {
		if (start == null || end == null || end < start) {
			throw new IllegalArgumentException();
		}
	}
}