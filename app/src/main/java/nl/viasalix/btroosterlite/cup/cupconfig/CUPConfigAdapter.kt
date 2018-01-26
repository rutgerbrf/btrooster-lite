/*  BTRooster Lite: Roosterapp voor Calvijn College
 *  Copyright (C) 2017 Rutger Broekhoff <rutger broekhoff three at gmail dot com>
 *                 and Jochem Broekhoff
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nl.viasalix.btroosterlite.cup.cupconfig

import android.content.Context
import android.os.Bundle
import android.support.annotation.IntRange
import android.support.annotation.NonNull
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.stepstone.stepper.Step
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter
import com.stepstone.stepper.viewmodel.StepViewModel

class CUPConfigAdapter(fm: FragmentManager, context: Context) : AbstractFragmentStepAdapter(fm, context) {
    override fun createStep(position: Int): Step {
        val step: Fragment = when (position) {
            0 -> CUPConfigFragment1()
            1 -> CUPConfigFragment2()
            2 -> CUPConfigFragment3()
            else -> CUPConfigFragment1()
        }

        val b = Bundle()

        b.putInt("0", position)
        step.arguments = b
        return step as Step
    }

    @NonNull
    override fun getViewModel(@IntRange(from = 0) position: Int): StepViewModel =
            StepViewModel.Builder(context)
                    .setTitle("BTRooster Lite")
                    .setEndButtonLabel(
                            if (position == count - 1)
                                "Klaar"
                            else
                                "Volgende"
                    ).setBackButtonLabel("Terug").create()

    // De hoeveelheid aan stappen
    override fun getCount() = 3
}