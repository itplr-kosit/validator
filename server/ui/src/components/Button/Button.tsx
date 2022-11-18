import clsx from "clsx";
import type { ButtonHTMLAttributes, DetailedHTMLProps, ReactNode } from "react";
import React from "react";
import type { ExtendProps } from "../util/types";
import styles from "./Button.module.css";

type HTMLButtonProps = DetailedHTMLProps<
	ButtonHTMLAttributes<HTMLButtonElement>,
	HTMLButtonElement
>;
type ButtonProps = ExtendProps<
	HTMLButtonProps,
	{
		children: ReactNode;
		type?: "button" | "submit" | "reset";
		className?: string;
		loading?: boolean;
	}
>;

function Button({
	children,
	type = "button",
	className,
	loading = false,
	...props
}: ButtonProps): JSX.Element {
	return (
		<button
			{...props}
			className={clsx(styles.button, loading && styles.loading, className)}
			// eslint-disable-next-line react/button-has-type
			type={type}
			aria-busy={loading}
		>
			<div className={styles.spinnerWrapper} aria-hidden>
				<div className={styles.spinner} />
			</div>
			{children}
		</button>
	);
}

export default Button;
