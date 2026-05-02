package com.course.service.impl;

import com.course.domain.Member;
import com.course.dto.registration.RegistrationResponse;
import com.course.enums.RegistrationResult;
import com.course.mapper.LectureMapper;
import com.course.mapper.MemberMapper;
import com.course.mapper.RegistrationMapper;
import com.course.service.RegistrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.course.enums.RegistrationResult.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationMapper registrationMapper;
    private final MemberMapper memberMapper;
    private final LectureMapper lectureMapper;


    @Override
    @Transactional
    public RegistrationResult registerLecture(String email, long lectureNo) throws Exception {
        Member member = memberMapper.findMemberByEmail(email);

        //이미 신청이 완료된 강의인지 확인
        int count = registrationMapper.checkIsAlreadyRegistered(member.getMemberNo(), lectureNo);
        if (count > 0) {
            //이미 신청된 강의일 경우
            return ALREADY_REGISTERED;
        }
        int updated = lectureMapper.checkCapacityAndIncreaseCurrentEnrollment(lectureNo);
        if (updated == 0) {
            //정원 초과인지 체크하면서 현 인원을 +1해서 실패했다면 0 리턴
            //실패시 정원초과 리턴
            return FULL;
        }
        registrationMapper.registerLecture(member.getMemberNo(), lectureNo);
        return SUCCESS;
    }


    @Override
    public List<RegistrationResponse> getRegistrationList(String email) throws Exception {
        Member member = memberMapper.findMemberByEmail(email);
        return registrationMapper.getRegistrationList(member.getMemberNo());
    }


    @Override
    @Transactional
    public void cancelRegistration(String email, long registrationNo) throws Exception {
        Member member = memberMapper.findMemberByEmail(email);
        // hard delete 이기 때문에 아예 삭제가 되기 전에 먼저 숫자를 감소시키고 삭제해야 함 순서가 중요
        lectureMapper.decreaseCurrentEnrollment(registrationNo);
        registrationMapper.cancelRegistration(member.getMemberNo(), registrationNo);


    }
}
