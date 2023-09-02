package com.teleostnacl.szu.login.fragment;

import static com.teleostnacl.szu.libs.utils.activity.LoginUtil.ARG_AUTO_FLAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.teleostnacl.common.android.context.ContextUtils;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.common.android.fragment.BaseLoadingFragment;
import com.teleostnacl.common.android.view.ViewUtils;
import com.teleostnacl.szu.login.R;
import com.teleostnacl.szu.login.databinding.FragmentLoginMainBinding;
import com.teleostnacl.szu.login.model.LoginModel;
import com.teleostnacl.szu.login.viewmodel.LoginViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class LoginMainFragment extends BaseLoadingFragment {
    private FragmentLoginMainBinding binding;

    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login_main, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Intent intent = requireActivity().getIntent();
        // 从Intent中获取是否自动登录 及 登录成功后需要进行的操作
        boolean autoLogin = intent.getBooleanExtra(ARG_AUTO_FLAG, true);

        showLoadingView(true);

        // 自动登录
        disposable.add(loginViewModel.loginByLast(autoLogin).subscribe(aBoolean -> {
            // 自动登录成功
            if (aBoolean) {
                requireActivity().finish();
            }
            // 自动登录失败
            else {
                // 有历史登录记录 设置最后一次登录的记录
                if (loginViewModel.loginModel != null) {
                    binding.setLoginModel(loginViewModel.loginModel);
                } else {
                    // 不显示spinner
                    ViewUtils.setInvisible(binding.loginUsernameSpinner);
                    binding.setLoginModel(loginViewModel.loginModel = new LoginModel());
                }

                initView();
            }
        }));
    }

    private void initView() {
        // 还未获取登录表单信息 则进行获取 否则显示view
        if (!loginViewModel.loginModel.hasGotten) {
            showLoadingView(false);
            // 获取登录表单信息
            disposable.add(loginViewModel.getInformation().subscribe(aBoolean -> showView()));
        } else {
            showView();
        }

        //是否显示密码单击事件
        binding.loginShowPasswordIcon.setOnClickListener(v -> {
            //记录当前光标位置
            int location = binding.loginPassword.getSelectionStart();

            // 隐藏密码时显示密码
            if (binding.loginPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
                binding.loginPassword.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance());
                binding.loginShowPasswordIcon.setImageResource(R.drawable.ic_login_password_show);
            }
            // 显示密码时 隐藏密码
            else {
                binding.loginPassword.setTransformationMethod(
                        PasswordTransformationMethod.getInstance());
                binding.loginShowPasswordIcon.setImageResource(R.drawable.ic_login_password_not_show);
            }


            binding.loginPassword.setSelection(location);
        });
        binding.loginPassword.setRelationViews(binding.loginShowPasswordIcon);

        //设置验证码图片单击事件
        binding.captchaImg.setOnClickListener(v -> loginViewModel.getCaptcha());

        //设置登录的操作
        disposable.add(Observable.create(emitter ->
                        binding.buttonLoginSubmit.setOnClickListener(v -> {
                            //判空操作
                            boolean flag = true;

                            //用户名为空
                            if (TextUtils.isEmpty(loginViewModel.loginModel.username)) {
                                ToastUtils.makeToast(R.string.login_need_username);
                                flag = false;
                            }

                            //密码为空
                            if (TextUtils.isEmpty(loginViewModel.loginModel.passwordInput)) {
                                ToastUtils.makeToast(R.string.login_need_password);
                                flag = false;
                            }

                            //需要验证码时验证码为空
                            if (loginViewModel.loginModel.needCaptcha &&
                                    TextUtils.isEmpty(loginViewModel.loginModel.captcha)) {
                                ToastUtils.makeToast(R.string.login_need_captcha);
                                flag = false;
                            }

                            if (flag) {
                                showLoadingView(true);
                                emitter.onNext(loginViewModel.loginModel);
                            }
                        }))
                .switchMap(o -> loginViewModel.loginByPassword())
                .subscribe(aBoolean -> {
                    //登录成功
                    if (aBoolean) {
                        requireActivity().finish();
                    } else {
                        hideLoadingView();
                    }
                }));

        //使用说明单击响应,启用浏览器浏览网页
        binding.loginHelp.setOnClickListener(v -> ContextUtils.startBrowserActivity(
                "https://www1.szu.edu.cn/v.asp?id=136"));

        //忘记密码单击响应事件,启用浏览器浏览网页
        binding.loginForgetPassword.setOnClickListener(v -> ContextUtils.startBrowserActivity(
                "https://authserver.szu.edu.cn/authserver/getBackPasswordMainPage.do"));


        //获取历史登录过的账户密码
        List<String> list = new ArrayList<>(loginViewModel.loginModelList.size() + 1);
        list.add(getString(R.string.login_new));

        for (int i = 1; i < loginViewModel.loginModelList.size() + 1; i++) {
            list.add(loginViewModel.loginModelList.get(i - 1).username);
        }
        ArrayAdapter<String> volumeAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.layout_login_spinner, list);
        volumeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.loginUsernameSpinner.setAdapter(volumeAdapter);
        binding.loginUsernameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isFirst = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 不在spinner提供的TextView显示文字
                view.setVisibility(View.GONE);

                if (isFirst) {
                    isFirst = false;
                    return;
                }
                // 新建
                if (position == 0) {
                    binding.setLoginModel(loginViewModel.loginModel = new LoginModel());
                } else {
                    LoginModel loginModel = loginViewModel.loginModelList.get(position - 1);

                    // 重复选择且没有修改 则提示移除该LoginModel
                    if (loginModel.equals(loginViewModel.loginModel)) {
                        new AlertDialog.Builder(requireContext())
                                .setMessage(getString(R.string.login_delete, loginModel.username))
                                .setPositiveButton(com.teleostnacl.common.android.R.string.delete, (dialog, which) -> {
                                    loginViewModel.removeLoginModel(loginModel);
                                    volumeAdapter.remove(loginModel.username);

                                    // 移除完之后为历史登录0 则隐藏Spinner
                                    if (loginViewModel.loginModelList.size() == 0) {
                                        ViewUtils.setInvisible(binding.loginUsernameSpinner);
                                    }
                                })
                                .setNegativeButton(com.teleostnacl.common.android.R.string.back, null)
                                .show();
                    } else {
                        binding.setLoginModel(loginViewModel.loginModel = loginModel.clone());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        binding.loginUsernameSpinner.setSelection(1);
    }

    /**
     * 展示页面
     */
    private void showView() {
        binding.getRoot().setVisibility(View.VISIBLE);
        binding.getRoot().post(this::hideLoadingView);
    }

    /**
     * 展示加载动画
     *
     * @param isLogin 控制文字展示 true - 正在登录中 false - 正在加载中
     */
    private void showLoadingView(boolean isLogin) {
        super.showLoadingView(com.teleostnacl.szu.libs.R.color.neumorphism_main_background_color,
                isLogin ? R.string.logging : com.teleostnacl.common.android.R.string.loading);
    }
}
