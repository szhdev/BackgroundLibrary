#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
BackgroundLibrary Live Template 生成脚本

从 attrs.xml 自动生成 Android Studio Live Template 文件 (BackgroundLibrary.xml)。
用于在 XML 布局中快速提示 BackgroundLibrary 的自定义属性。

使用方式:
    # 直接生成（覆盖 BackgroundLibrary.xml）
    python scripts/generate_live_templates.py
    # 仅查看变更报告，不写入文件
    python scripts/generate_live_templates.py --dry-run
    # 指定输入/输出路径
    python scripts/generate_live_templates.py --input path/to/attrs.xml --output path/to/output.xml
"""

import argparse
import os
import re
import sys
import xml.etree.ElementTree as ET
from collections import OrderedDict

# ============================================================
# 属性描述映射表
# ============================================================

# 基础属性 -> 中文描述 (去掉 bl_ 前缀后匹配)
DESCRIPTION_MAP = {
    "shape": "shape类型",
    "shape_alpha": "shape透明度",
    "solid_color": "填充颜色",
    "corners_radius": "圆角半径",
    "corners_bottomLeftRadius": "左下圆角",
    "corners_bottomRightRadius": "右下圆角",
    "corners_topLeftRadius": "左上圆角",
    "corners_topRightRadius": "右上圆角",
    "corners_leftRadius": "左侧圆角",
    "corners_topRadius": "上侧圆角",
    "corners_rightRadius": "右侧圆角",
    "corners_bottomRadius": "下侧圆角",
    "gradient_angle": "渐变角度",
    "gradient_centerX": "渐变中心X",
    "gradient_centerY": "渐变中心Y",
    "gradient_centerColor": "渐变中间颜色",
    "gradient_endColor": "渐变结束颜色",
    "gradient_startColor": "渐变开始颜色",
    "gradient_gradientRadius": "渐变半径",
    "gradient_type": "渐变类型",
    "gradient_useLevel": "是否使用level",
    "padding_left": "左内边距",
    "padding_top": "上内边距",
    "padding_right": "右内边距",
    "padding_bottom": "下内边距",
    "size_width": "宽度",
    "size_height": "高度",
    "stroke_width": "边框宽度",
    "stroke_color": "边框颜色",
    "stroke_dashWidth": "边框虚线宽度",
    "stroke_dashGap": "边框虚线间隔",
    "stroke_gradient_startColor": "描边渐变开始颜色",
    "stroke_gradient_centerColor": "描边渐变中间颜色",
    "stroke_gradient_endColor": "描边渐变结束颜色",
    "stroke_gradient_angle": "描边渐变角度",
    "stroke_position": "边框位置",
    "shadow_color": "阴影颜色",
    "shadow_size": "阴影大小",
    "shadow_offsetX": "阴影X偏移",
    "shadow_offsetY": "阴影Y偏移",
    "ripple_enable": "是否使用ripple",
    "ripple_color": "ripple颜色",
    "function": "方法名",
    "position": "位置",
    "oneshot": "是否单次播放",
    "anim_auto_start": "帧动画自动播放",
    "duration": "动画时长",
    "text_gradient_startColor": "文字渐变开始颜色",
    "text_gradient_endColor": "文字渐变结束颜色",
    "text_gradient_orientation": "文字渐变方向",
    "unpressed_color": "未按下颜色",
    "pressed_color": "按下颜色",
    # multi selector
    "multi_selector1": "多状态选择器1",
    "multi_selector2": "多状态选择器2",
    "multi_selector3": "多状态选择器3",
    "multi_selector4": "多状态选择器4",
    "multi_selector5": "多状态选择器5",
    "multi_selector6": "多状态选择器6",
    "multi_text_selector1": "多文本状态选择器1",
    "multi_text_selector2": "多文本状态选择器2",
    "multi_text_selector3": "多文本状态选择器3",
    "multi_text_selector4": "多文本状态选择器4",
    "multi_text_selector5": "多文本状态选择器5",
    "multi_text_selector6": "多文本状态选择器6",
    # button drawable
    "checked_button_drawable": "checked状态button drawable",
    "unChecked_button_drawable": "unChecked状态button drawable",
}

# 状态前缀映射
STATE_PREFIX_MAP = {
    "pressed": "按下状态",
    "checked": "选中状态",
    "enabled": "启用状态",
    "focused": "聚焦状态",
    "selected": "选择状态",
    "checkable": "可选中状态",
    "activated": "激活状态",
    "active": "活动状态",
    "expanded": "展开状态",
    "unPressed": "未按下状态",
    "unChecked": "未选中状态",
    "unEnabled": "未启用状态",
    "unFocused": "未聚焦状态",
    "unSelected": "未选择状态",
    "unCheckable": "不可选中状态",
    "unActivated": "未激活状态",
    "unActive": "未活动状态",
    "unExpanded": "未展开状态",
}

# 基础属性的中文描述 (用于带状态前缀的属性)
BASE_ATTR_DESC = {
    "solid_color": "填充颜色",
    "stroke_color": "边框颜色",
    "drawable": "drawable",
    "hovered": "悬停drawable",
    "activated": "激活drawable",
    "textColor": "文字颜色",
    "gradient_angle": "渐变角度",
    "gradient_centerX": "渐变中心X",
    "gradient_centerY": "渐变中心Y",
    "gradient_centerColor": "渐变中间颜色",
    "gradient_endColor": "渐变结束颜色",
    "gradient_startColor": "渐变开始颜色",
    "gradient_gradientRadius": "渐变半径",
    "gradient_type": "渐变类型",
    "gradient_useLevel": "是否使用level",
}


def get_description(attr_name: str) -> str:
    """
    根据属性名生成中文描述。

    规则:
    1. 去掉 bl_ 前缀
    2. 检查是否有状态前缀
    3. 查找基础属性映射
    4. 如果匹配不到，使用属性名本身
    """
    # 去掉 bl_ 前缀
    name = attr_name[3:] if attr_name.startswith("bl_") else attr_name

    # 先检查完整名称是否在映射表中
    if name in DESCRIPTION_MAP:
        return DESCRIPTION_MAP[name]

    # 检查 duration_item* 和 frame_drawable_item* 模式
    duration_match = re.match(r"duration_item(\d+)", name)
    if duration_match:
        return f"第{duration_match.group(1)}帧动画时长"

    frame_match = re.match(r"frame_drawable_item(\d+)", name)
    if frame_match:
        return f"第{frame_match.group(1)}帧动画drawable"

    # 检查状态前缀
    for prefix, state_desc in STATE_PREFIX_MAP.items():
        if name.startswith(prefix + "_"):
            rest = name[len(prefix) + 1:]
            # 查找基础属性描述
            if rest in BASE_ATTR_DESC:
                return f"{state_desc}{BASE_ATTR_DESC[rest]}"
            # 尝试在 DESCRIPTION_MAP 中查找
            if rest in DESCRIPTION_MAP:
                return f"{state_desc}{DESCRIPTION_MAP[rest]}"
            return f"{state_desc}{rest}"

    # 都匹配不到，返回属性名本身
    return name


def parse_attrs_xml(file_path: str) -> list:
    """
    解析 attrs.xml，提取所有 attr 定义。

    返回: [(attr_name, format_type, styleable_name), ...]
    """
    tree = ET.parse(file_path)
    root = tree.getroot()

    attrs = []
    seen = set()  # 避免重复（同一属性可能在多个 styleable 中出现）

    for styleable in root.findall("declare-styleable"):
        styleable_name = styleable.get("name", "")
        for attr in styleable.findall("attr"):
            attr_name = attr.get("name", "")
            if not attr_name or attr_name in seen:
                continue

            # 确定 format
            fmt = attr.get("format", "")
            # 如果有 enum 子元素，标记为 enum
            if attr.findall("enum"):
                if fmt:
                    fmt = fmt + "|enum"
                else:
                    fmt = "enum"
            # 如果有 flag 子元素，标记为 flags
            elif attr.findall("flag"):
                if fmt:
                    fmt = fmt + "|flags"
                else:
                    fmt = "flags"

            seen.add(attr_name)
            attrs.append((attr_name, fmt, styleable_name))

    return attrs


def is_enum_or_flags(fmt: str) -> bool:
    """判断属性格式是否包含 enum 或 flags"""
    if not fmt:
        return False
    parts = fmt.split("|")
    return "enum" in parts or "flags" in parts


def generate_template_xml(attrs: list) -> str:
    """
    根据属性列表生成 BackgroundLibrary.xml 内容。

    参数:
        attrs: [(attr_name, format_type, styleable_name), ...]

    返回:
        完整的 XML 字符串
    """
    # 按属性名排序
    sorted_attrs = sorted(attrs, key=lambda x: x[0])

    lines = ['<templateSet group="BackgroundLibrary">']

    for attr_name, fmt, _ in sorted_attrs:
        description = get_description(attr_name)
        # 转义 description 中的特殊 XML 字符
        description = description.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace('"', "&quot;")

        # toReformat: enum/flags 类型设为 false，其他设为 true
        to_reformat = "false" if is_enum_or_flags(fmt) else "true"

        value = f'app:{attr_name}=&quot;$value$&quot;'

        lines.append(f'  <template name="{attr_name}" value="{value}" description="{description}" toReformat="{to_reformat}" toShortenFQNames="true">')
        lines.append('    <variable name="value" expression="" defaultValue="" alwaysStopAt="true" />')
        lines.append('    <context>')
        lines.append('      <option name="XML" value="true" />')
        lines.append('    </context>')
        lines.append('  </template>')

    lines.append('</templateSet>')

    return "\n".join(lines) + "\n"


def parse_existing_xml(file_path: str) -> dict:
    """
    解析现有的 BackgroundLibrary.xml，提取 template 信息。

    返回: {name: description, ...}
    """
    if not os.path.exists(file_path):
        return {}

    templates = {}
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        for template in root.findall("template"):
            name = template.get("name", "")
            desc = template.get("description", "")
            if name:
                templates[name] = desc
    except ET.ParseError:
        print(f"警告: 无法解析现有文件 {file_path}，将作为全新文件处理")
    return templates


def generate_report(old_templates: dict, new_attrs: list) -> str:
    """
    生成对比报告。

    参数:
        old_templates: 现有文件中的 {name: description}
        new_attrs: 新生成的属性列表

    返回:
        格式化的报告字符串
    """
    new_templates = {}
    for attr_name, fmt, _ in new_attrs:
        new_templates[attr_name] = get_description(attr_name)

    old_names = set(old_templates.keys())
    new_names = set(new_templates.keys())

    added = sorted(new_names - old_names)
    removed = sorted(old_names - new_names)
    common = old_names & new_names
    desc_changed = sorted([n for n in common if old_templates[n] != new_templates[n]])

    report_lines = []
    report_lines.append("=" * 60)
    report_lines.append("BackgroundLibrary.xml 变更报告")
    report_lines.append("=" * 60)
    report_lines.append("")
    report_lines.append(f"总属性数: {len(new_names)} (原有: {len(old_names)})")
    report_lines.append("")

    # 新增
    report_lines.append(f"--- 新增模板 ({len(added)} 个) ---")
    if added:
        for name in added:
            report_lines.append(f"  + {name}: {new_templates[name]}")
    else:
        report_lines.append("  (无)")
    report_lines.append("")

    # 删除
    report_lines.append(f"--- 已删除模板 ({len(removed)} 个) ---")
    if removed:
        for name in removed:
            report_lines.append(f"  - {name}: {old_templates[name]}")
    else:
        report_lines.append("  (无)")
    report_lines.append("")

    # 描述变更
    report_lines.append(f"--- 描述更新 ({len(desc_changed)} 个) ---")
    if desc_changed:
        for name in desc_changed:
            report_lines.append(f"  * {name}:")
            report_lines.append(f"      旧: {old_templates[name]}")
            report_lines.append(f"      新: {new_templates[name]}")
    else:
        report_lines.append("  (无)")
    report_lines.append("")
    report_lines.append("=" * 60)

    return "\n".join(report_lines)


def main():
    parser = argparse.ArgumentParser(
        description="从 attrs.xml 生成 BackgroundLibrary Live Template 文件"
    )
    parser.add_argument(
        "--input", "-i",
        default=None,
        help="attrs.xml 路径 (默认: library/src/main/res/values/attrs.xml)"
    )
    parser.add_argument(
        "--output", "-o",
        default=None,
        help="输出文件路径 (默认: BackgroundLibrary.xml)"
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="仅输出报告，不写入文件"
    )
    args = parser.parse_args()

    # 确定项目根目录 (脚本在 scripts/ 下)
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(script_dir)

    # 确定输入输出路径
    input_path = args.input or os.path.join(
        project_root, "library", "src", "main", "res", "values", "attrs.xml"
    )
    output_path = args.output or os.path.join(project_root, "BackgroundLibrary.xml")

    # 检查输入文件
    if not os.path.exists(input_path):
        print(f"错误: 找不到输入文件 {input_path}")
        sys.exit(1)

    print(f"输入: {input_path}")
    print(f"输出: {output_path}")
    print()

    # 解析 attrs.xml
    attrs = parse_attrs_xml(input_path)
    print(f"从 attrs.xml 中解析到 {len(attrs)} 个属性")

    # 解析现有文件
    old_templates = parse_existing_xml(output_path)

    # 生成报告
    report = generate_report(old_templates, attrs)
    print()
    print(report)

    # 生成 XML
    if not args.dry_run:
        xml_content = generate_template_xml(attrs)
        with open(output_path, "w", encoding="utf-8", newline="\n") as f:
            f.write(xml_content)
        print(f"\n已生成: {output_path}")
    else:
        print("\n[dry-run 模式] 未写入文件")


if __name__ == "__main__":
    main()
