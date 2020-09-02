package jpabook.jpashop.api;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

	private final MemberService memberService;
	
	@GetMapping("/api/v1/members")
	public List<Member> membersV1(){
		//api를 [ ]로 응답하면 api 확장성에 어려움이 있으니 api는 { } 형태로 응답한다.
		return memberService.findMembers();
	}
	
	@GetMapping("/api/v2/members")
	public Result<List<MemberDto>> membersV2(){
		List<Member> findMembers = memberService.findMembers();
		List<MemberDto> members = findMembers.stream()
							.map(m -> new MemberDto(m.getName(), m.getAddress()))
							.collect(Collectors.toList());
			
		return new Result<List<MemberDto>>(members.size(), members);
	}
	
	@Data
	@AllArgsConstructor
	static class Result<T> {
		private int size;
		private T data;
	}
	
	@Data
	@AllArgsConstructor
	static class MemberDto {
		private String name;
		private Address address;
	}
	
	@PostMapping("/api/v1/members")
	public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
		Long id = memberService.join(member);
		return new CreateMemberResponse(id);
	}
	
	//api 스펙을 한눈에 알아볼 수 있고, Entity 속성 변화에도 유연하게 대처 가능하도록 요청과 응답 모두 dto를 쓴다.
	@PostMapping("/api/v2/members")
	public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
		
		Member member = new Member();
		member.setName(request.getName());
		
		Long id = memberService.join(member);
		return new CreateMemberResponse(id);
	}
	
	@Data
	static class CreateMemberRequest {
		private String name;
	}
	
	@Data
	static class CreateMemberResponse {
		private Long id;

		public CreateMemberResponse(Long id) {
			this.id = id;
		}
	}
	
	@PutMapping("/api/v2/members/{id}")
	public UpdateMemberResponse updateMemberV2(
			@PathVariable("id") Long id,
			@RequestBody @Valid UpdateMemberRequest request) {
		
		memberService.update(id, request.getName());
		Member findMember = memberService.findOne(id);
		return new UpdateMemberResponse(findMember.getId(), findMember.getName());
	}
	
	@Data
	static class UpdateMemberRequest {
		private String name;
	}
	
	@Data
	@AllArgsConstructor
	static class UpdateMemberResponse {
		private Long id;
		private String name;
	}
}
