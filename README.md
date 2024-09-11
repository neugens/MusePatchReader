This is a very quick and dirty converter for the Moog Muse patches into something that can be used via MIDI or just to study the patches.

At the time of this writing, the Muse has a couple of annoying quirks: the pots are jumping instead of latching onto the actual value and it doesn't allow to read the value of any of the parameters.
Furthermore, it doesn't allow to set a global pitch control, so you can only use whatever is configured in the patch at the moment, but again, you can't read it unless is in the mod menu.
For patches you are generating, is not a big deal, since the Muse transmit MIDI you just need to be careful to take note of the final value you use, but for patches already stored you are out of luck, except that the patch format is in clear text so we cna stufy it :)

There's indeed some interesting issue with MIDI since Muse uses regualar MIDI and not NPRN so there's less resolution (only 128 values to represent what the synth can do by storing floating values from 0 to 1 in the patch definition file), but it's good enough to understand the patch and I doubt there's any practical difference, say, when hearing a filter cutoff for example.

Included, there's a "normalised.txt" which is a copy of all the default patches using these normalised values, the values are also sorted by their key so they are not all over the place when looking at oscillators tuning and such.

Of course, you can't run this as is, since I happily hardcoded the path where I saved the files but that should be a simple fix, so I hope this information is useful.

Over time, I want to try adding some logic to detune the patches any numbner of semitones, since what I really miss from this synth is the ability to set a reference tuning. So much for a performance dedicated machine, isn't it?
