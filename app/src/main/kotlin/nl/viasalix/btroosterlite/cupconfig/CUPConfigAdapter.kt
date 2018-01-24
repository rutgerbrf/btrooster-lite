package nl.viasalix.btroosterlite.cupconfig

import android.support.v4.app.FragmentManager
import android.content.Context
import android.os.Bundle
import android.support.annotation.IntRange
import android.support.annotation.NonNull
import android.support.v4.app.Fragment
import android.util.Log
import com.stepstone.stepper.Step
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter
import com.stepstone.stepper.viewmodel.StepViewModel

class CUPConfigAdapter(fm: FragmentManager, context: Context) : AbstractFragmentStepAdapter(fm, context) {

    override fun createStep(position: Int): Step {
        val step = when (position) {
            0 -> CUPConfigFragment1()
            1 -> CUPConfigFragment2()
            2 -> CUPConfigFragment3()
            else -> CUPConfigFragment1()
        } as Fragment

        Log.d("position", Integer.toString(position))
        val b = Bundle()
        b.putInt("0", position)
        step.arguments = b
        return step as Step
    }

    @NonNull
    override fun getViewModel(@IntRange(from = 0) position: Int): StepViewModel {
        return StepViewModel.Builder(context)
                .setTitle("BTRooster Lite")
                .create()
    }

    override fun getCount() = 3
}