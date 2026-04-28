package com.course.service.impl;

import com.course.domain.Member;
import com.course.domain.Registration;
import com.course.dto.registration.RegistrationResponse;
import com.course.mapper.MemberMapper;
import com.course.mapper.RegistrationMapper;
import com.course.service.MemberService;
import com.course.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationMapper registrationMapper;
    private final MemberMapper memberMapper;
    public static final int AVAILABLE = 1, UNAVAILABLE = 0 ;


    @Override
    public int registerLecture(String email, long lectureNo) throws Exception {
        Member member = memberMapper.findMemberByEmail(email);

        //이미 신청이 완료된 강의일 경우 튕겨내야 함
        int count = registrationMapper.checkIsAlreadyRegistered(member.getMemberNo(),lectureNo);
        if(count >0) {
            //이미 신청된 강의일 경우
            return UNAVAILABLE;
        }else{
            registrationMapper.registerLecture(member.getMemberNo(),lectureNo);
            return AVAILABLE;
        }

    }



    @Override
    public List<RegistrationResponse> getRegistrationList(String email) throws Exception {
        Member member = memberMapper.findMemberByEmail(email);
        return registrationMapper.getRegistrationList(member.getMemberNo());
    }



    @Override
    public void cancelRegistration(String email, long registrationNo) throws Exception {
        Member member = memberMapper.findMemberByEmail(email);
        registrationMapper.cancelRegistration(member.getMemberNo(),registrationNo);


    }
}
