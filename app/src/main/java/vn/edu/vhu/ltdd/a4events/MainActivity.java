package vn.edu.vhu.ltdd.a4events;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "A4_241A010130";
    private static final String KEY_LICHSU = "key_lich_su_phep_tinh";

    private EditText edtSoA, edtSoB, edtCanNang, edtChieuCao;
    private TextView tvKetQua, tvBmi, tvPhanLoai, tvLichSu;

    private ArrayList<String> lichSuList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // ---- Ánh xạ view ----
        edtSoA = findViewById(R.id.edtSoA);
        edtSoB = findViewById(R.id.edtSoB);
        tvKetQua = findViewById(R.id.tvKetQua);
        tvLichSu = findViewById(R.id.tvLichSu);
        edtCanNang = findViewById(R.id.edtCanNang);
        edtChieuCao = findViewById(R.id.edtChieuCao);
        tvBmi = findViewById(R.id.tvBmi);
        tvPhanLoai = findViewById(R.id.tvPhanLoai);

        Button btnCong = findViewById(R.id.btnCong);
        Button btnTru = findViewById(R.id.btnTru);
        Button btnNhan = findViewById(R.id.btnNhan);
        Button btnChia = findViewById(R.id.btnChia);
        Button btnPhanTram = findViewById(R.id.btnPhanTram);
        Button btnDaoDau = findViewById(R.id.btnDaoDau);
        Button btnXoa = findViewById(R.id.btnXoa);
        Button btnTinhBmi = findViewById(R.id.btnTinhBmi);

        // ---- Bài NC1: Thêm nút % và Đảo dấu ± ----
        btnCong.setOnClickListener(v -> tinhToan('+'));
        btnTru.setOnClickListener(v -> tinhToan('-'));
        btnPhanTram.setOnClickListener(v -> tinhToan('%'));
        btnDaoDau.setOnClickListener(v -> daoDau());

        View.OnClickListener chung = v -> {
            int id = v.getId();
            if (id == R.id.btnNhan) {
                tinhToan('*');
            } else if (id == R.id.btnChia) {
                tinhToan('/');
            }
        };
        btnNhan.setOnClickListener(chung);
        btnChia.setOnClickListener(chung);

        btnXoa.setOnClickListener(v -> xoaTrang());

        // ---- Bài NC2: Khôi phục lịch sử khi xoay màn hình ----
        if (savedInstanceState != null) {
            ArrayList<String> savedList = savedInstanceState.getStringArrayList(KEY_LICHSU);
            if (savedList != null) {
                lichSuList = savedList;
            }
        }
        capNhatLichSuHienThi();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // NC2: Lưu danh sách lịch sử phép tính
        outState.putStringArrayList(KEY_LICHSU, lichSuList);
    }

    // =============== MÁY TÍNH ===============

    private void tinhToan(char phepToan) {
        String chuoiA = edtSoA.getText().toString().trim();
        String chuoiB = edtSoB.getText().toString().trim();

        // Bước 1: kiểm tra rỗng và báo lỗi ngay trên ô nhập
        if (chuoiA.isEmpty()) {
            edtSoA.setError(getString(R.string.err_empty));
            edtSoA.requestFocus();
            return;
        }
        if (chuoiB.isEmpty()) {
            edtSoB.setError(getString(R.string.err_empty));
            edtSoB.requestFocus();
            return;
        }

        // Bước 2: chuyển chuỗi sang số, bẫy lỗi định dạng
        double a, b;

        try {
            a = Double.parseDouble(chuoiA.replace(',', '.'));
        } catch (NumberFormatException e) {
            Log.e(TAG, "Dữ liệu nhập ô A không phải số: '" + chuoiA + "'", e);
            edtSoA.setError(getString(R.string.err_not_number));
            edtSoA.requestFocus();
            Toast.makeText(this, R.string.err_not_number, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            b = Double.parseDouble(chuoiB.replace(',', '.'));
        } catch (NumberFormatException e) {
            Log.e(TAG, "Dữ liệu nhập ô B không phải số: '" + chuoiB + "'", e);
            edtSoB.setError(getString(R.string.err_not_number));
            edtSoB.requestFocus();
            Toast.makeText(this, R.string.err_not_number, Toast.LENGTH_SHORT).show();
            return;
        }

        // Bước 3: xử lý trường hợp đặc biệt
        if ((phepToan == '/' || phepToan == '%') && b == 0) {
            edtSoB.setError(getString(R.string.err_divide_zero));
            Toast.makeText(this, R.string.err_divide_zero, Toast.LENGTH_SHORT).show();
            return;
        }

        double ketQua;
        switch (phepToan) {
            case '+': ketQua = a + b; break;
            case '-': ketQua = a - b; break;
            case '*': ketQua = a * b; break;
            case '%': ketQua = a % b; break;
            default:  ketQua = a / b; break;
        }

        String ketQuaStr = String.format(Locale.getDefault(), "%.2f %c %.2f = %.2f",
                a, phepToan, b, ketQua);
        tvKetQua.setText(ketQuaStr);
        Log.d(TAG, "Phép tính: " + ketQuaStr);

        // NC2: Lưu phép tính vào lịch sử (tối đa 5 phép tính gần nhất)
        lichSuList.add(0, ketQuaStr);
        if (lichSuList.size() > 5) {
            lichSuList.remove(lichSuList.size() - 1);
        }
        capNhatLichSuHienThi();
    }

    // NC1: Đảo dấu (nhân -1 số hiện tại)
    private void daoDau() {
        EditText edtTarget = edtSoB.hasFocus() ? edtSoB : edtSoA;

        String valStr = edtTarget.getText().toString().trim();
        if (valStr.isEmpty()) {
            edtTarget.setError(getString(R.string.err_empty));
            edtTarget.requestFocus();
            Toast.makeText(this, R.string.err_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double val = Double.parseDouble(valStr.replace(',', '.'));
            val = val * -1;
            if (val == (long) val) {
                edtTarget.setText(String.format(Locale.getDefault(), "%d", (long) val));
            } else {
                edtTarget.setText(String.format(Locale.getDefault(), "%s", val));
            }
            edtTarget.setError(null);
        } catch (NumberFormatException e) {
            edtTarget.setError(getString(R.string.err_not_number));
            edtTarget.requestFocus();
            Toast.makeText(this, R.string.err_not_number, Toast.LENGTH_SHORT).show();
        }
    }

    // NC2: Cập nhật TextView hiển thị lịch sử
    private void capNhatLichSuHienThi() {
        if (lichSuList == null || lichSuList.isEmpty()) {
            tvLichSu.setText(R.string.history_empty);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lichSuList.size(); i++) {
                sb.append(i + 1).append(". ").append(lichSuList.get(i));
                if (i < lichSuList.size() - 1) {
                    sb.append("\n");
                }
            }
            tvLichSu.setText(sb.toString());
        }
    }

    private void xoaTrang() {
        edtSoA.setText("");
        edtSoB.setText("");
        edtSoA.setError(null);
        edtSoB.setError(null);
        tvKetQua.setText(R.string.result_placeholder);
        edtSoA.requestFocus();
    }
}
