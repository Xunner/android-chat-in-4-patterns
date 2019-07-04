package nju.androidchat.client.mvvm0.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nju.androidchat.client.BR;


/**
 * @author Xunner
 */
@AllArgsConstructor
@NoArgsConstructor
public class NumberInvisibleObservable extends BaseObservable {
    @Getter
    @Bindable
    private String numberInvisible;

    public void setNumberInvisible(String numberInvisible) {
        this.numberInvisible = numberInvisible;
        notifyPropertyChanged(BR._all);
    }
}
