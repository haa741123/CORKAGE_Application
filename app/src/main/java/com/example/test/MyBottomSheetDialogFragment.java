package com.example.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MyBottomSheetDialogFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 여기에서 사용할 레이아웃을 인플레이트합니다. 예를 들어 fragment_my_bottom_sheet.xml이라는 레이아웃 파일을 사용한다고 가정합니다.
        return inflater.inflate(R.layout.fragment_my_bottom_sheet, container, false);
    }
}
