package top.codestyle.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.codestyle.mapper.LibraryInfoLikeMapper;
import top.codestyle.model.dto.DoLikeRequest;
import top.codestyle.model.entity.LibraryInfo;
import top.codestyle.model.entity.LibraryInfoLike;
import top.codestyle.model.entity.User;
import top.codestyle.service.LibraryInfoLikeService;
import top.codestyle.service.LibraryInfoService;
import top.codestyle.service.UserService;


@Service
@Slf4j
@RequiredArgsConstructor
public class LibraryInfoLikeServiceImpl extends ServiceImpl<LibraryInfoLikeMapper, LibraryInfoLike>
        implements LibraryInfoLikeService {

    private final UserService userService;
    private final LibraryInfoService libraryInfoService;
    private final TransactionTemplate transactionTemplate;

    @Override
    public Boolean doLike(DoLikeRequest doLikeRequest, HttpServletRequest request) {
        if (doLikeRequest == null || doLikeRequest.getLibraryInfoId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);

        //加锁
        synchronized (loginUser.getId().toString().intern()) {
            //编程式事务
            return transactionTemplate.execute(status -> {
                Long libraryInfoId = doLikeRequest.getLibraryInfoId();
                boolean exists = this.lambdaQuery()
                        .eq(LibraryInfoLike::getUserId, loginUser.getId())
                        .eq(LibraryInfoLike::getInfoId, libraryInfoId)
                        .exists();
                if (exists) {
                    throw new RuntimeException("用户已点赞");
                }
                boolean update = libraryInfoService.lambdaUpdate()
                        .eq(LibraryInfo::getId, libraryInfoId)
                        .setSql("totalLikeCount = totalLikeCount + 1")
                        .update();
                LibraryInfoLike libraryInfoLike = new LibraryInfoLike();
                libraryInfoLike.setUserId(loginUser.getId());
                libraryInfoLike.setInfoId(libraryInfoId);
                return update && this.save(libraryInfoLike);
            });
        }


    }

    @Override
    public Boolean undoLike(DoLikeRequest doLikeRequest, HttpServletRequest request) {
        if (doLikeRequest == null || doLikeRequest.getLibraryInfoId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);

        //加锁
        synchronized (loginUser.getId().toString().intern()) {
            //编程式事务
            return transactionTemplate.execute(status -> {
                Long libraryInfoId = doLikeRequest.getLibraryInfoId();
                LibraryInfoLike libraryInfoLike = this.lambdaQuery()
                        .eq(LibraryInfoLike::getUserId, loginUser.getId())
                        .eq(LibraryInfoLike::getInfoId, libraryInfoId)
                        .one();
                if (libraryInfoLike == null) {
                    throw new RuntimeException("用户未点赞");
                }
                boolean update = libraryInfoService.lambdaUpdate()
                        .eq(LibraryInfo::getId, libraryInfoId)
                        .setSql("totalLikeCount = totalLikeCount - 1")
                        .update();
                return update && this.removeById(libraryInfoLike.getId());
            });
        }
    }
}
