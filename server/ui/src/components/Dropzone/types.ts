import type { ReactNode, RefAttributes } from "react";
import type {
	DropEvent,
	DropzoneProps as ReactDropzoneProps,
	DropzoneRef,
} from "react-dropzone";
import type { ExtendProps } from "../util/types";

export interface RejectionType {
	file: File;
}

export type DropzoneProps = ExtendProps<
	ReactDropzoneProps & RefAttributes<DropzoneRef>,
	{
		children?: ReactNode;
		className?: string;
		activeClassName?: string;
		multiple?: boolean;
		hasSelectedFiles?: boolean;
		name?: string;
		onDrop: (accepted: File[], rejections: File[], event: DropEvent) => void;
	}
>;
