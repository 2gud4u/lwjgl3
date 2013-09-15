/* 
 * Copyright LWJGL. All rights reserved.
 * License terms: http://lwjgl.org/license.php
 */
package org.lwjgl.system.macosx.templates

import org.lwjgl.generator.*
import org.lwjgl.system.macosx.*

fun dlfcn() = "DynamicLinkLoader".nativeClass(MACOSX_PACKAGE) {
	nativeImport (
		"MacOSXLWJGL.h",
		"<dlfcn.h>"
	)

	javaDoc("Native bindings to <dlfcn.h>.")

	val Modes = IntConstant.block(
		"The {@code mode} argument to {@link #dlopen} contains one of the following:",

		"RTLD_LAZY" _ 0x1,
		"RTLD_NOW" _ 0x2,
		"RTLD_LOCAL" _ 0x4,
		"RTLD_GLOBAL" _ 0x8
	).toJavaDocLinks()

	LongConstant.block(
		"Special-handle values for {@link #dlsym}:",

		"RTLD_NEXT" _ -1.L,
		"RTLD_DEFAULT" _ -2.L,
		"RTLD_SELF" _ -3.L,
		"RTLD_MAIN_ONLY" _ -5.L
	)

	// skipping dladdr

	voidptr.func(
		"dlopen",
		"""
		Loads and links a dynamic library or bundle.

		This function examines the Mach-O file specified by path. If the image is compatible with the current process and has not already been loaded into the
		process, the image is loaded and linked. If the image contains initializer functions, they are executed before this function returns.

		Subsequent calls to {@code dlopen} to load the same image return the same handle, but the internal reference count for the handle is incremented.
		Therefore, all {@code dlopen} calls must be balanced with {@link #dlclose} calls.

		For efficiency, the {@link #RTLD_LAZY} binding mode is preferred over {@link #RTLD_NOW}. However, using {@link #RTLD_NOW} ensures that any undefined
		symbols are discovered during the call to {@code dlopen}.

		The dynamic loader looks in the paths specified by a set of environment variables, and in the process's current directory, when it searches for a
		library. These paths are called dynamic loader search paths. The environment variables are {@code LD_LIBRARY_PATH}, {@code DYLD_LIBRARY_PATH}, and
		{@code DYLD_FALLBACK_LIBRARY_PATH}. The default value of {@code DYLD_FALLBACK_LIBRARY_PATH} (used when this variable is not set), is
		{@code ${'$'}HOME/lib;/usr/local/lib;/usr/lib}.

		The order in which the search paths are searched depends on whether path is a filename (it does not contain a slash) or a pathname (it contains at least
		one slash).

		When path is a filename, the dynamic loader searches for the library in the search paths in the following order:
		${ul(
			"${'$'}LD_LIBRARY_PATH",
			"${'$'}DYLD_LIBRARY_PATH",
			"The process's working directory",
			"${'$'}DYLD_FALLBACK_LIBRARY_PATH"
		)}
		When path is a pathname, the dynamic loader searches for the library in the search paths in the following order:
		${ul(
			"${'$'}DYLD_LIBRARY_PATH",
			"The given pathname",
			"${'$'}DYLD_FALLBACK_LIBRARY_PATH using the filename"
		)}
		""",

		mods(const, nullable) _ charASCII_p.IN("path", "path to the image to open"),
		int.IN(
			"mode",
			"""
			specifies when the loaded image's external symbols are bound to their definitions in dependent libraries (lazy or at load time) and the visibility
			of the image's exported symbols (global or local). The value of this parameter is made up by ORing one binding behavior value with one visibility
			specification value.

			The following values specify the binding behavior:
			${ul(
				"{@link #RTLD_LAZY} (default): Each external symbol reference is bound the first time it's used.",
				"{@link #RTLD_NOW}: All external symbol references are bound immediately."
			)}

			The following values specify external symbol visibility:
			${ul(
				"""
				{@link #RTLD_GLOBAL} (default): The loaded image's exported symbols are available to any images that use a flat namespace or to calls to
				dlsym when using a special handle (see {@link #dlsym} for details).
				""",
				"""
				{@link #RTLD_LOCAL}: The loaded image's exported symbols are generally hidden. They are available only to {@link #dlsym} invocations that
				use the handle returned by this function.
				"""
			)}
			"""
		)
	)

	(const _ charASCII_p).func(
		"dlerror",
		"""
		Provides diagnostic information corresponding to problems with calls to {@link #dlopen}, {@link #dlsym}, and {@link #dlclose} in the same thread.

		When there's a problem to report, this function returns a pointer to a null-terminated string describing the problem. Otherwise, this function returns
		$NULL.

		Each call to {@code dlerror} resets its diagnostic buffer. If a program needs to keep a record of past error messages, it must store them itself.
		Subsequent calls to {@code dlerror} in the same thread with no calls to {@link #dlopen}, {@link #dlsym}, or {@link #dlclose}, return $NULL.
		"""
	)

	voidptr.func(
		"dlsym",
		"""
		Returns the address of a symbol.

		The value of handle specifies what images this function searches for to locate the symbol specified by the symbol parameter. The following table
		describes the possible values for the handle parameter:
		${table(
			tr(th("Handle value"), th("Search scope"), align = "left"),
			tr(td("{@code dlopen} handle"), td("Image associated with the {@link #dlopen} handle."), align = "left"),
			tr(
				td("{@link #RTLD_DEFAULT}"),
				td("Every dependent library or {@link #RTLD_GLOBAL}–opened library in the current process, in the order they were loaded."),
				align = "left"
			),
			tr(
				td("{@link #RTLD_NEXT}"),
				td("Dependent libraries that were loaded after the one calling this function. Libraries opened with {@link #dlopen} are not searched."),
				align = "left"
			)
		)}

		Unlike in the NS... functions, the symbol parameter doesn't require a leading underscore to be part of the symbol name.
		""",

		voidptr.IN(
			"handle",
			"""
			a handle obtained by a call to {@link #dlopen}, or a special handle. If the handle was obtained by a call to {@link #dlopen}, it must not have been
			closed with a call to {@link #dlclose}. These are the possible special-handle values: {@link #RTLD_DEFAULT}, and {@link #RTLD_NEXT}.
			"""
		),
		const _ charASCII_p.IN("name", "the null-terminated character string containing the C name of the symbol being sought")
	)

	int.func(
		"dlclose",
		"""
		Closes a dynamic library or bundle.

		This function decreases the reference count of the image referenced by handle. When the reference count for handle becomes 0, the termination routines
		in the image are called, and the image is removed from the address space of the current process. After that point, handle is rendered invalid.
		""",

		voidptr.IN("handle", "a handle obtained through a call to {@link #dlopen}.")
	)

}